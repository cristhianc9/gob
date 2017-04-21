/**
 * 
 */
package ec.gob.sri.fedatarios.intranet.web.controlador.planificacion.puntofijo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import ec.gob.sri.fedatarios.api.constantes.ConstanteAplicacion;
import ec.gob.sri.fedatarios.api.enumeracion.TipoTareaPuntoFijoEnum;
import ec.gob.sri.fedatarios.api.exception.ControladaException;
import ec.gob.sri.fedatarios.api.utils.CollectionsUtil;
import ec.gob.sri.fedatarios.api.utils.LoggerUtil;
import ec.gob.sri.fedatarios.intranet.web.controlador.planificacion.GestionTareaPuntoFijo;
import ec.gob.sri.fedatarios.persistencia.entity.control.SujetoIntervencionParticipante;

/**
 * @author cfcg070314
 *
 */
public class ProgramadorTarea extends GestionTareaPuntoFijo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final LoggerUtil log = new LoggerUtil(Logger.getLogger(ProgramadorTarea.class));

	private static final String nombreClaseEstiloSujetoControl = "sujetocontrol";

	private static final Map<Integer, String> mapaClaseEstiloSujetoControl = new HashMap<Integer, String>();

	private static final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");

	protected ScheduleModel eventModel;

	protected ScheduleModel lazyEventModel;

	protected Date fechaInicial;

	protected ScheduleEvent event;

	@PostConstruct
	public void iniciarProgramadorTarea() {
		eventModel = new DefaultScheduleModel();
		crearMapaEstilosClaseSujetoControl();
	}

	private void crearMapaEstilosClaseSujetoControl() {
		for (Integer contadorEstiloClase = 1; contadorEstiloClase <= 10; contadorEstiloClase++) {
			mapaClaseEstiloSujetoControl.put(contadorEstiloClase, ConstanteAplicacion.SIN_ESPACIO);
		}
	}

	public ScheduleModel getEventModel() {
		return eventModel;
	}

	public void setEventModel(ScheduleModel eventModel) {
		this.eventModel = eventModel;
	}

	public ScheduleModel getLazyEventModel() {
		return lazyEventModel;
	}

	public void setLazyEventModel(ScheduleModel lazyEventModel) {
		this.lazyEventModel = lazyEventModel;
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	public ScheduleEvent getEvent() {
		return event;
	}

	public void setEvent(ScheduleEvent event) {
		this.event = event;
	}

	public void iniciarModelo(List<SujetoIntervencionParticipante> listaTareas) {
		eventModel.clear();
		log.debug("===>>\n ingresa a iniciar modelo");
		for (SujetoIntervencionParticipante sujetoIntervencionParticipante : listaTareas) {
			DefaultScheduleEvent defaultScheduleEvent = new DefaultScheduleEvent();
			log.debug("===>> sujetoIntervencionParticipante: ruc: "
					+ sujetoIntervencionParticipante.getRucEstablecimiento() + " ejecucion: "
					+ sujetoIntervencionParticipante.getFechaEjecucion() + " ini: "
					+ sujetoIntervencionParticipante.getHoraInicio() + " fin:"
					+ sujetoIntervencionParticipante.getHoraFin());

			Date fechaEjecucion = sujetoIntervencionParticipante.getFechaEjecucion();
			Date fechaInicio = obtenerFechaHora(fechaEjecucion, sujetoIntervencionParticipante.getHoraInicio());
			Date fechaFin = obtenerFechaHora(fechaEjecucion, sujetoIntervencionParticipante.getHoraFin());

			String tituloEvento = formatoHora.format(fechaInicio) + " - " + formatoHora.format(fechaFin) + " "
					+ sujetoIntervencionParticipante.getFuenteSujetoControl().getFuenteEstablecimiento()
							.getNombreComercial();
			defaultScheduleEvent.setId(null);
			defaultScheduleEvent.setTitle(tituloEvento);
			defaultScheduleEvent.setStartDate(fechaInicio);
			defaultScheduleEvent.setEndDate(fechaFin);
			defaultScheduleEvent.setData(sujetoIntervencionParticipante);
			defaultScheduleEvent.setEditable(permitirEdicion(sujetoIntervencionParticipante));
			if (sujetoIntervencionParticipante.getNombreEstiloClaseContribuyente() != null) {
				verificarEstiloClaseUtilizado(sujetoIntervencionParticipante.getNombreEstiloClaseContribuyente(),
						sujetoIntervencionParticipante.getFuenteSujetoControl().getInformacionRuc().getNumeroRuc());
				defaultScheduleEvent.setStyleClass(sujetoIntervencionParticipante.getNombreEstiloClaseContribuyente());
			}
			eventModel.addEvent(defaultScheduleEvent);
		}
	}

	private void verificarEstiloClaseUtilizado(String nombreEstiloClaseContribuyente, String numeroRuc) {
		Integer valorClaseDisponible = 1;
		for (Map.Entry<Integer, String> elementoEstiloClase : mapaClaseEstiloSujetoControl.entrySet()) {
			valorClaseDisponible = elementoEstiloClase.getKey();
			if (nombreEstiloClaseContribuyente
					.equals(nombreClaseEstiloSujetoControl.concat(valorClaseDisponible.toString()))) {
				elementoEstiloClase.setValue(numeroRuc);
			}
		}
	}

	private static Date obtenerFechaHora(Date fecha, Date hora) {

		Calendar calendarFecha = Calendar.getInstance();
		calendarFecha.setTime(fecha);

		Calendar calendarHora = Calendar.getInstance();
		calendarHora.setTime(hora);

		Calendar fechaHora = calendarFecha;

		fechaHora.set(Calendar.HOUR_OF_DAY, calendarHora.get(Calendar.HOUR_OF_DAY));
		fechaHora.set(Calendar.MINUTE, calendarHora.get(Calendar.MINUTE));
		fechaHora.set(Calendar.SECOND, 0);
		fechaHora.set(Calendar.MILLISECOND, 0);

		return fechaHora.getTime();
	}

	public void addEvent(ActionEvent actionEvent) {

		boolean tieneErrores = false;

		if (getAsignacionTareasPuntoFijoTareasVo() == null || CollectionsUtil.cadenaEstaNulaOVacia(
				getAsignacionTareasPuntoFijoTareasVo().getCadenaFuenteSujetoControlSeleccionado())) {
			addErrorMessage("Debe seleccionar el sujeto a intervenir", null);
			tieneErrores = true;
		}

		Date horaInicio = obtenerFechaHora(getSujetoIntervencionParticipante().getFechaEjecucion(),
				getSujetoIntervencionParticipante().getHoraInicio());
		Date horaFin = obtenerFechaHora(getSujetoIntervencionParticipante().getFechaEjecucion(),
				getSujetoIntervencionParticipante().getHoraFin());

		getSujetoIntervencionParticipante().setHoraInicio(horaInicio);
		getSujetoIntervencionParticipante().setHoraFin(horaFin);
		getSujetoIntervencionParticipante().setNombreEstiloClaseContribuyente(generarEstiloClaseSujetoControl(
				getSujetoIntervencionParticipante().getFuenteSujetoControl().getInformacionRuc().getNumeroRuc()));

		try {
			guardarSujetoIntervencionParticipante(getTipoTareaPuntoFijoEnum().name());
		} catch (ControladaException e) {
			addErrorMessage(e.getMessage(), null);
			tieneErrores = true;
		}
		setFechaInicial(getSujetoIntervencionParticipante().getFechaEjecucion());
		if (procesarEvento) {
			agregarActualizarEvento();
		} else {
			FacesContext.getCurrentInstance().validationFailed();
			tieneErrores = true;
		}

		if (!tieneErrores) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("eventDialog.hide()");
			context.execute("wvProgramadorTareas.update()");
		}
	}

	private String generarEstiloClaseSujetoControl(String numeroRuc) {
		Integer valorClaseDisponible = 1;
		for (Map.Entry<Integer, String> elementoEstiloClase : mapaClaseEstiloSujetoControl.entrySet()) {
			valorClaseDisponible = elementoEstiloClase.getKey();
			if (elementoEstiloClase.getValue().equals(ConstanteAplicacion.SIN_ESPACIO)) {
				elementoEstiloClase.setValue(numeroRuc);
				return nombreClaseEstiloSujetoControl.concat(valorClaseDisponible.toString());
			}
			if (numeroRuc.equals(elementoEstiloClase.getValue())) {
				return nombreClaseEstiloSujetoControl.concat(valorClaseDisponible.toString());
			}

		}
		return null;
	}

	private void agregarActualizarEvento() {
		DefaultScheduleEvent eventDefault = (DefaultScheduleEvent) event;
		eventDefault.setTitle(formatoHora.format(getSujetoIntervencionParticipante().getHoraInicio()) + " - "
				+ formatoHora.format(getSujetoIntervencionParticipante().getHoraFin()) + " "
				+ getSujetoIntervencionParticipante().getFuenteSujetoControl().getFuenteEstablecimiento()
						.getNombreComercial());
		eventDefault.setStartDate(getSujetoIntervencionParticipante().getHoraInicio());
		eventDefault.setEndDate(getSujetoIntervencionParticipante().getHoraFin());
		eventDefault.setStyleClass(getSujetoIntervencionParticipante().getNombreEstiloClaseContribuyente());
		if (eventDefault.getId() == null) {
			eventModel.addEvent(eventDefault);
		} else {
			eventModel.updateEvent(eventDefault);
		}
		event = new DefaultScheduleEvent();
	}

	public void onEventSelect(SelectEvent selectEvent) {
		setTipoTareaPuntoFijoEnum(TipoTareaPuntoFijoEnum.TRANSACCION);
		event = (ScheduleEvent) selectEvent.getObject();
		if (event != null) {
			itemSujetoIntervencionParticipante = (SujetoIntervencionParticipante) event.getData();
			if (itemSujetoIntervencionParticipante.getFechaEjecucion() == null) {
				itemSujetoIntervencionParticipante.setFechaEjecucion(event.getStartDate());
			}
			if (itemSujetoIntervencionParticipante.getHoraInicio() == null) {
				itemSujetoIntervencionParticipante.setHoraInicio(event.getStartDate());
			}
			if (itemSujetoIntervencionParticipante.getHoraFin() == null) {
				itemSujetoIntervencionParticipante.setHoraFin(event.getEndDate());
			}

			cargarSujetoIntervencionParticipanteSeleccionado(itemSujetoIntervencionParticipante);
			DefaultScheduleEvent defaultEvent = (DefaultScheduleEvent) event;
			defaultEvent.setEditable(permitirEdicion(itemSujetoIntervencionParticipante));

		}
	}

	public void onDateSelect(SelectEvent selectEvent) {
		setTipoTareaPuntoFijoEnum(TipoTareaPuntoFijoEnum.TRANSACCION);
		asignarTareaPuntoFijo(getTipoTareaPuntoFijoEnum().name());
		event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
		sujetoIntervencionParticipante.setFechaEjecucion(event.getStartDate());
		sujetoIntervencionParticipante.setHoraInicio(event.getStartDate());
		sujetoIntervencionParticipante.setHoraFin(event.getEndDate());

	}

	public void onEventMove(ScheduleEntryMoveEvent event) {
		setTipoTareaPuntoFijoEnum(TipoTareaPuntoFijoEnum.TRANSACCION);
		if (event != null) {
			aplicarCambios(event.getScheduleEvent(), event.getDayDelta(), event.getMinuteDelta());
		}
	}

	public void onEventResize(ScheduleEntryResizeEvent event) {
		setTipoTareaPuntoFijoEnum(TipoTareaPuntoFijoEnum.TRANSACCION);
		if (event != null) {
			aplicarCambios(event.getScheduleEvent(), event.getDayDelta(), event.getMinuteDelta());
		}
	}

	/**
	 * @param event
	 */
	private void aplicarCambios(ScheduleEvent evento, int dias, int minutos) {
		itemSujetoIntervencionParticipante = (SujetoIntervencionParticipante) evento.getData();
		cargarSujetoIntervencionParticipanteSeleccionado(itemSujetoIntervencionParticipante);
		getSujetoIntervencionParticipante().setFechaEjecucion(evento.getStartDate());
		getSujetoIntervencionParticipante().setHoraInicio(evento.getStartDate());
		getSujetoIntervencionParticipante().setHoraFin(evento.getEndDate());
		try {
			guardarSujetoIntervencionParticipante(getTipoTareaPuntoFijoEnum().name());
		} catch (ControladaException e) {
			addErrorMessage(e.getMessage(), null);
		}

		if (!(procesarEvento && permitirEdicion(getSujetoIntervencionParticipante()))) {
			revertirCambios(evento, dias, minutos);
		}
	}

	private void revertirCambios(ScheduleEvent eventEditado, int dias, int minutos) {
		DefaultScheduleEvent eventoOriginal = (DefaultScheduleEvent) eventEditado;
		Calendar fechaInicio = Calendar.getInstance();
		Calendar fechaFin = Calendar.getInstance();
		fechaInicio.setTime(eventoOriginal.getStartDate());
		fechaFin.setTime(eventoOriginal.getEndDate());
		if (dias != 0) {
			fechaInicio.add(Calendar.DAY_OF_MONTH, -1 * dias);
			fechaFin.add(Calendar.DAY_OF_MONTH, -1 * dias);
		}
		if (minutos != 0) {
			fechaInicio.add(Calendar.MINUTE, -1 * minutos);
			fechaFin.add(Calendar.MINUTE, -1 * minutos);
		}

		eventoOriginal.setStartDate(fechaInicio.getTime());
		eventoOriginal.setEndDate(fechaFin.getTime());

		getSujetoIntervencionParticipante().setFechaEjecucion(fechaInicio.getTime());
		getSujetoIntervencionParticipante().setHoraInicio(fechaInicio.getTime());
		getSujetoIntervencionParticipante().setHoraFin(fechaFin.getTime());

	}

	protected void eliminarEvento() {
		eventModel.deleteEvent(event);
	}

}
