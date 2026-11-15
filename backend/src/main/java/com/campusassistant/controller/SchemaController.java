package com.campusassistant.controller;

import com.campusassistant.dto.SchemaDTO;
import com.campusassistant.service.SchemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schema")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping
    public ResponseEntity<SchemaDTO> getSchema() {
        return ResponseEntity.ok(schemaService.getSchemaDTO());
    }
}
