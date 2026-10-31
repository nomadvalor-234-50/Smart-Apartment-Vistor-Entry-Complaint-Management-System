package com.project.savecms.controller;
import com.project.savecms.entity.Flat;
import com.project.savecms.service.FlatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flats")
public class FlatController {

    private final FlatService flatService;

    public FlatController(FlatService flatService) {
        this.flatService = flatService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFlat(
            @RequestBody Flat flat,
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Flat savedFlat = flatService.createFlat(extractToken(authorizationHeader), flat);

            response.put("message", "Flat created successfully.");
            response.putAll(toFlatResponse(savedFlat));

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (SecurityException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (IllegalStateException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        } catch (IllegalArgumentException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFlats(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            List<Map<String, Object>> flats = flatService
                    .getAllFlats(extractToken(authorizationHeader))
                    .stream()
                    .map(this::toFlatResponse)
                    .toList();

            response.put("message", "Flats retrieved successfully.");
            response.put("flats", flats);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (SecurityException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (IllegalStateException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFlat(
            @PathVariable Long id,
            @RequestBody Flat flat,
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Flat updatedFlat = flatService.updateFlat(
                    extractToken(authorizationHeader), id, flat);

            response.put("message", "Flat updated successfully.");
            response.putAll(toFlatResponse(updatedFlat));

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (SecurityException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (IllegalStateException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        } catch (IllegalArgumentException exception) {
            response.put("message", exception.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid or expired session token.");
        }

        return authorizationHeader.substring(7);
    }

    private Map<String, Object> toFlatResponse(Flat flat) {
        Map<String, Object> flatResponse = new LinkedHashMap<>();

        flatResponse.put("flatId", flat.getId());
        flatResponse.put("flatNumber", flat.getFlatNumber());
        flatResponse.put("blockName", flat.getBlockName());
        flatResponse.put("floorNumber", flat.getFloorNumber());
        flatResponse.put("numberOfRooms", flat.getNumberOfRooms());

        return flatResponse;
    }
}