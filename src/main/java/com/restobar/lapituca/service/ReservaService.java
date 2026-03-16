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

        List<Reserva> reservasSolapadas =
                reservaRepository.findReservasSolapadas(
                        fecha,
                        inicio,
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
        Long transaccionId = reserva.getTransaccion() != null ? reserva.getTransaccion().getId() : null;

        return new ReservaResponse(
                reserva.getId(),
                reserva.getFecha_reserva(),
                reserva.getHora_reserva(),
                reserva.getNum_personas(),
                reserva.getEstado(),
                reserva.getUsuario().getId(),
                grupoId,
                transaccionId,
                reserva.getFechaHora_registro()
        );
    }

    public List<MesasDisponiblesResponse> verMesasDisponibles(LocalDate fecha, LocalTime hora){

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

    /*
    public ReservaResponse actualizar(Long id, ReservaRequest request){

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reserva no encontrada"));

        if("CANCELADO".equalsIgnoreCase(reserva.getEstado())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,
                    "No se puede modificar una reserva cancelada");
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Usuario no encontrado"));

        Grupo grupo = null;

        if(request.getGrupoId()!=null){
            grupo = grupoRepository.findById(request.getGrupoId())
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                            "Grupo no encontrado"));
        }

        reserva.setFecha_reserva(request.getFechaReserva());
        reserva.setHora_reserva(request.getHoraReserva());
        reserva.setNum_personas(request.getNumPersonas());
        reserva.setUsuario(usuario);
        reserva.setGrupo(grupo);

        return mapToResponse(reservaRepository.save(reserva));
    }*/

    public List<ReservaResponse> listar(){

        return reservaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReservaResponse obtenerPorId(Long id){

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reserva no encontrada"));

        return mapToResponse(reserva);
    }

    public void cancelar(Long id){

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Reserva no encontrada"));

        reserva.setEstado("CANCELADO");

        reservaRepository.save(reserva);
    }

    /* Mejora para producción (muy recomendable) acelera findReservasSolapadas muchísimo cuando tengas muchas reservas.
    CREATE INDEX idx_reserva_fecha_hora
    ON reserva (fecha_reserva, hora_reserva);*/

}