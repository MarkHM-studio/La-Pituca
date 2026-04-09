package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.ReservaRequest;
import com.restobar.lapituca.dto.response.MesasDisponiblesResponse;
import com.restobar.lapituca.dto.response.ReservaResponse;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final SucursalRepository sucursalRepository;
    private final MesaRepository mesaRepository;
    private final DetalleMesaRepository detalleMesaRepository;
    private final ComprobanteRepository comprobanteRepository;

    @Transactional
    public ReservaResponse crear(ReservaRequest request){
        actualizarEstadosAutomaticos();

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Usuario no encontrado"));

        Sucursal sucursal = sucursalRepository.findById(request.getSucursalId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Sucursal no encontrada"));

        if(request.getFechaReserva().isAfter(LocalDate.now().plusDays(7))){
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Solo se puede reservar hasta 1 semana en el futuro");
        }

        //Validacion del horario de atención  17:00 → 02:00 (día siguiente)
        LocalTime hora = request.getHoraReserva();

        boolean horarioValido =
                !hora.isBefore(LocalTime.of(17,0)) ||
                        !hora.isAfter(LocalTime.of(2,0));

        if(!horarioValido){
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Horario fuera de atención (17:00 - 02:00)"
            );
        }

        //Evita reservas con hora pasada
        if(request.getFechaReserva().isEqual(LocalDate.now())){

            if(request.getHoraReserva().isBefore(LocalTime.now())){
                throw new ApiException(
                        ErrorCode.BUSINESS_RULE_ERROR,
                        "No se puede reservar en una hora pasada"
                );
            }
        }

        //Validar que en mesasId no hallan Ids duplicadas
        if(request.getMesasId().size() != new HashSet<>(request.getMesasId()).size()){
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Mesas duplicadas en la solicitud"
            );
        }

        int capacidadMesas = request.getMesasId().size() * 3;

        if(request.getNumPersonas() > capacidadMesas){
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Una mesa admite máximo 3 personas. Seleccione más mesas");
        }
        /*
        List<Mesa> mesas = mesaRepository.findAllById(request.getMesasId());*/
        List<Mesa> mesas = mesaRepository.findMesasForUpdate(request.getMesasId());

        if(mesas.size() != request.getMesasId().size()){
            throw new ApiException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "Una o más mesas no existen");
        }

        validarMesasDisponibles(
                request.getFechaReserva(),
                request.getHoraReserva(),
                request.getMesasId()
        );

        Grupo grupo = new Grupo();
        grupo.setNombre("Reserva");
        grupo.setEstado("ACTIVO");
        grupo.setTipoGrupo(2);

        grupoRepository.save(grupo);

        //Guardamos tantos detalle mesa como mesas a selecionado el cliente
        List<DetalleMesa> detalles = new ArrayList<>();

        for(Mesa mesa : mesas){

            DetalleMesa detalle = new DetalleMesa();
            detalle.setGrupo(grupo);
            detalle.setMesa(mesa);

            detalles.add(detalle);
        }
        detalleMesaRepository.saveAll(detalles);

        Comprobante comprobante = new Comprobante();
        comprobante.setTotal(BigDecimal.ZERO);
        comprobante.setIGV(BigDecimal.ZERO);
        comprobante.setEstado("ABIERTO");
        comprobante.setSucursal(sucursal);
        comprobante.setGrupo(grupo);

        comprobanteRepository.save(comprobante);

        Reserva reserva = new Reserva();
        reserva.setFecha_reserva(request.getFechaReserva());
        reserva.setHora_reserva(request.getHoraReserva());
        reserva.setNum_personas(request.getNumPersonas());
        reserva.setUsuario(usuario);
        reserva.setGrupo(grupo);
        reserva.setEstado("ESPERANDO PAGO");
        reserva.setFechaHora_expiracionPago(
                LocalDateTime.now().plusMinutes(10)
        );

        reservaRepository.save(reserva);

        return mapToResponse(reserva);
    }

    private void validarMesasDisponibles(
            LocalDate fecha,
            LocalTime hora,
            Set<Long> mesasId){

        LocalTime inicio = hora;
        LocalTime fin = hora.plusHours(1);
        LocalTime inicioMenosUnaHora = inicio.minusHours(1);

        List<Reserva> reservasSolapadas =
                reservaRepository.findReservasSolapadas(
                        fecha,
                        inicioMenosUnaHora,
                        fin,
                        mesasId
                );

        if(!reservasSolapadas.isEmpty()){
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Una o más mesas ya están reservadas en ese horario"
            );
        }
    }

    private ReservaResponse mapToResponse(Reserva reserva){

        Long grupoId = reserva.getGrupo() != null ? reserva.getGrupo().getId() : null;
        List<Long> mesasIds = reserva.getGrupo() == null || reserva.getGrupo().getDetalleMesas() == null
                ? List.of()
                : reserva.getGrupo().getDetalleMesas().stream()
                .map(detalle -> detalle.getMesa().getId())
                .toList();

        List<Long> transaccionesIds = reserva.getTransacciones() == null
                ? List.of()
                : reserva.getTransacciones().stream()
                .map(Transaccion::getId)
                .toList();

        Long ultimaTransaccionId = reserva.getTransacciones() == null
                ? null
                : reserva.getTransacciones().stream()
                .max(Comparator.comparing(Transaccion::getFechaActualizacion))
                .map(Transaccion::getId)
                .orElse(null);

        return new ReservaResponse(
                reserva.getId(),
                reserva.getFecha_reserva(),
                reserva.getHora_reserva(),
                reserva.getNum_personas(),
                reserva.getEstado(),
                reserva.getUsuario().getId(),
                grupoId,
                mesasIds,
                ultimaTransaccionId,
                transaccionesIds,
                reserva.getFechaHora_registro(),
                reserva.getFechaHora_verificacionReserva(),
                reserva.getUsuarioVerificador() != null ? reserva.getUsuarioVerificador().getId() : null
        );
    }

    public List<MesasDisponiblesResponse> verMesasDisponibles(LocalDate fecha, LocalTime hora){
        actualizarEstadosAutomaticos();

        LocalTime fin = hora.plusHours(1);

        List<Mesa> mesas = mesaRepository.findAll();

        List<Reserva> reservas = reservaRepository
                .findReservasEnRango(fecha,hora,fin);

        Set<Long> mesasReservadas = reservas.stream()
                .flatMap(r -> r.getGrupo().getDetalleMesas().stream())
                .map(d -> d.getMesa().getId())
                .collect(Collectors.toSet());

        return mesas.stream()
                .map(m -> new MesasDisponiblesResponse(
                        m.getId(),
                        m.getNombre(),
                        mesasReservadas.contains(m.getId())
                ))
                .toList();

    }


    public ReservaResponse actualizar(Long id, ReservaRequest request){
        actualizarEstadosAutomaticos();

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Reserva no encontrada"));

        if ("CANCELADO".equalsIgnoreCase(reserva.getEstado()) || "EXPIRADO".equalsIgnoreCase(reserva.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede actualizar una reserva cancelada o expirada");
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario no encontrado"));

        if(request.getFechaReserva().isAfter(LocalDate.now().plusDays(7))){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Solo se puede reservar hasta 1 semana en el futuro");
        }

        LocalTime hora = request.getHoraReserva();
        boolean horarioValido = !hora.isBefore(LocalTime.of(17,0)) || !hora.isAfter(LocalTime.of(2,0));
        if(!horarioValido){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Horario fuera de atención (17:00 - 02:00)");
        }

        if(request.getFechaReserva().isEqual(LocalDate.now()) && request.getHoraReserva().isBefore(LocalTime.now())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede reservar en una hora pasada");
        }

        if(request.getMesasId().size() != new HashSet<>(request.getMesasId()).size()){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Mesas duplicadas en la solicitud");
        }

        int capacidadMesas = request.getMesasId().size() * 3;
        if(request.getNumPersonas() > capacidadMesas){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Una mesa admite máximo 3 personas. Seleccione más mesas");
        }

        List<Reserva> reservasSolapadas = reservaRepository.findReservasSolapadas(
                request.getFechaReserva(),
                request.getHoraReserva().minusHours(1),
                request.getHoraReserva().plusHours(1),
                request.getMesasId()
        ).stream().filter(r -> !r.getId().equals(id)).toList();

        if(!reservasSolapadas.isEmpty()){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Una o más mesas ya están reservadas en ese horario");
        }

        List<Mesa> mesas = mesaRepository.findMesasForUpdate(request.getMesasId());
        if(mesas.size() != request.getMesasId().size()){
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Una o más mesas no existen");
        }

        Grupo grupo = reserva.getGrupo();
        detalleMesaRepository.deleteAll(detalleMesaRepository.findByGrupo_Id(grupo.getId()));

        List<DetalleMesa> nuevosDetalles = mesas.stream().map(mesa -> {
            DetalleMesa detalle = new DetalleMesa();
            detalle.setGrupo(grupo);
            detalle.setMesa(mesa);
            return detalle;
        }).toList();
        detalleMesaRepository.saveAll(nuevosDetalles);

        reserva.setFecha_reserva(request.getFechaReserva());
        reserva.setHora_reserva(request.getHoraReserva());
        reserva.setNum_personas(request.getNumPersonas());
        reserva.setUsuario(usuario);

        return mapToResponse(reservaRepository.save(reserva));
    }

    public List<ReservaResponse> listar(){
        actualizarEstadosAutomaticos();

        return reservaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReservaResponse obtenerPorId(Long id){
        actualizarEstadosAutomaticos();

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reserva no encontrada"));

        return mapToResponse(reserva);
    }

    public List<ReservaResponse> listarPorUsername(String username) {
        actualizarEstadosAutomaticos();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario no encontrado"));

        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getUsuario() != null && reserva.getUsuario().getId().equals(usuario.getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public ReservaResponse obtenerPorIdParaUsername(Long id, String username) {
        actualizarEstadosAutomaticos();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario no encontrado"));

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Reserva no encontrada"));

        if (reserva.getUsuario() == null || !reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "No puedes acceder a una reserva de otro usuario");
        }

        return mapToResponse(reserva);
    }

    public void cancelar(Long id, String username, boolean esCliente){

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reserva no encontrada"));

        if (esCliente) {
            if (reserva.getUsuario() == null || !username.equalsIgnoreCase(reserva.getUsuario().getUsername())) {
                throw new ApiException(ErrorCode.FORBIDDEN, "No puedes cancelar la reserva de otro usuario");
            }
        }

        if ("CANCELADO".equalsIgnoreCase(reserva.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "La reserva ya está cancelada");
        }

        if ("NO_SHOW".equalsIgnoreCase(reserva.getEstado())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede cancelar una reserva marcada como no_show");
        }

        reserva.setEstado("CANCELADO");
        reserva.setFechaHora_verificacionReserva(null);
        reserva.setUsuarioVerificador(null);

        reservaRepository.save(reserva);
    }

    public ReservaResponse verificarReserva(Long id, String username) {
        actualizarEstadosAutomaticos();

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Reserva no encontrada"));

        if (!"PAGADO".equalsIgnoreCase(reserva.getEstado())) {
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Solo se pueden verificar reservas pagadas"
            );
        }

        if (reserva.getFechaHora_verificacionReserva() != null) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "La reserva ya fue verificada");
        }

        LocalDateTime fechaHoraReserva = LocalDateTime.of(reserva.getFecha_reserva(), reserva.getHora_reserva());
        LocalDateTime limiteTolerancia = fechaHoraReserva.plusMinutes(15);

        if (LocalDateTime.now().isAfter(limiteTolerancia)) {
            reserva.setEstado("NO_SHOW");
            reservaRepository.save(reserva);
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "La reserva superó la tolerancia de 15 minutos y quedó como no_show"
            );
        }

        Usuario recepcionista = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario no encontrado"));

        reserva.setFechaHora_verificacionReserva(LocalDateTime.now());
        reserva.setUsuarioVerificador(recepcionista);

        return mapToResponse(reservaRepository.save(reserva));
    }

    private void actualizarReservasExpiradas() {
        reservaRepository.marcarReservasExpiradas();
    }

    private void actualizarReservasNoShow() {
        LocalTime horaLimite = LocalTime.now().minusMinutes(15);
        reservaRepository.marcarReservasNoShow(horaLimite);
    }

    private void actualizarEstadosAutomaticos() {
        actualizarReservasExpiradas();
        actualizarReservasNoShow();
    }

    /* Mejora para producción (muy recomendable) acelera findReservasSolapadas muchísimo cuando tengas muchas reservas.
    CREATE INDEX idx_reserva_fecha_hora
    ON reserva (fecha_reserva, hora_reserva);*/

}