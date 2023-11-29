package com.petarvukcevic.elib.controllers;

import com.petarvukcevic.elib.dto.command.CategoryCommand;
import com.petarvukcevic.elib.dto.command.CategoryUpdateCommand;
import com.petarvukcevic.elib.dto.query.CategoryQuery;
import com.petarvukcevic.elib.errors.ValidationException;
import com.petarvukcevic.elib.errors.validators.CategoryValidator;
import com.petarvukcevic.elib.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryValidator categoryValidator;

    @GetMapping("{id}")
    public ResponseEntity<CategoryQuery> findOneById(@PathVariable("id") Integer id) {
        CategoryQuery categoryQuery = categoryService.findOneById(id);

        return categoryQuery != null
                ? new ResponseEntity<>(categoryQuery, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping()
    public ResponseEntity<List<CategoryQuery>> findAll()
    {
        List<CategoryQuery> categories = categoryService.findAll();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping("/add-new")
    public ResponseEntity<CategoryQuery> createCategory(@RequestBody @Valid CategoryCommand categoryCommand)
            throws ValidationException
    {
        Errors potentialErrors = new BeanPropertyBindingResult(categoryCommand, "categoryCommand");
        ValidationUtils.invokeValidator(categoryValidator, categoryCommand, potentialErrors);

        if (potentialErrors.hasErrors())
        {
            throw new ValidationException(potentialErrors);
        }

        // supports -> CategoryValidator
        // validate -> categoryValidator

        CategoryQuery categoryQuery = categoryService.create(categoryCommand);
        return new ResponseEntity<>(categoryQuery, HttpStatus.CREATED);
    }

    @PutMapping()
    public ResponseEntity<Void> update(@RequestBody CategoryUpdateCommand categoryUpdateCommand)
    {
        if (categoryUpdateCommand.getId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        categoryService.update(categoryUpdateCommand);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Integer id)
    {
        categoryService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
