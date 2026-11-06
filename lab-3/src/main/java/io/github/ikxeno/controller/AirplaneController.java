package io.github.ikxeno.controller;

import io.github.ikxeno.airplane.AirplaneEntity;
import io.github.ikxeno.service.AirplaneService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(AirplaneController.BASE_PATH)
public final class AirplaneController {
    static final String BASE_PATH = "/api/airplanes";

    private final AirplaneService service;

    public AirplaneController(AirplaneService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreatedAirplaneResponse> save(
            @Valid @RequestBody AirplaneRequest request) {
        int id = service.save(request.model(), request.seats());
        return ResponseEntity.created(URI.create(BASE_PATH + "/" + id))
                .body(new CreatedAirplaneResponse(id));
    }

    @GetMapping("/{id}")
    public AirplaneEntity findById(@PathVariable int id) {
        return service.findById(id);
    }

    @GetMapping("/search")
    public AirplaneEntity findByModel(@RequestParam String model) {
        return service.findByModel(model);
    }

    @GetMapping
    public List<AirplaneEntity> findAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable int id,
            @Valid @RequestBody AirplaneRequest request) {
        service.update(
                new AirplaneEntity(id, request.model(), request.seats()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
