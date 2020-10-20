package hansol9.rest.events;

import hansol9.rest.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EventValidator eventValidator;

//    @PostMapping("/api/events")
    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors) {
//    public ResponseEntity createEvent(@RequestBody Event event) {
        if (errors.hasErrors()) {
//            return ResponseEntity.badRequest().body(errors);
            return badRequest(errors);
        }

        eventValidator.validate(eventDTO, errors);
        if (errors.hasErrors()) {
//            return ResponseEntity.badRequest().body(errors);
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDTO, Event.class);
        event.update();
        Event savedEvent = this.eventRepository.save(event);
//        URI createUri = linkTo(methodOn(EventController.class).createEvent()).slash("{id}").toUri();
//        URI createUri = linkTo(EventController.class).slash("{id}").toUri();
        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(savedEvent.getId());
        URI createUri = selfLinkBuilder.toUri();

//        return ResponseEntity.created(createUri).build();
//        return ResponseEntity.created(createUri).body(event);
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
//        eventResource.add(selfLinkBuilder.withSelfRel());
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(createUri).body(eventResource);
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        Page<Event> page = this.eventRepository.findAll(pageable);
//        PagedResources<Resource<Event>> pagedResources = assembler.toResource(page);
        var pagedResources = assembler.toResource(page, e -> new EventResource(e));
        pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }

    private ResponseEntity<ErrorsResource> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }


}
