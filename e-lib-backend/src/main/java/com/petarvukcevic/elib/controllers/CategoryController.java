package com.petarvukcevic.elib.controllers;

import com.petarvukcevic.elib.dto.command.CategoryCommand;
import com.petarvukcevic.elib.dto.command.CategoryUpdateCommand;
import com.petarvukcevic.elib.dto.query.CategoryQuery;
import com.petarvukcevic.elib.entities.Book;
import com.petarvukcevic.elib.entities.Category;
import com.petarvukcevic.elib.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("categories")
public class CategoryController {

    private final CategoryService categoryService;

//    @GetMapping("{id}")
//    public ResponseEntity<Category> findOneById(@PathVariable("id") Integer id) {
//        Category category = categoryService.findOneById(id);
//
//        return category != null
//                ? new ResponseEntity<>(category, HttpStatus.OK)
//                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
//    }

    @GetMapping()
    public ResponseEntity<List<CategoryQuery>> findAll()
    {
        List<CategoryQuery> categories = categoryService.findAll();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping("/add-new")
    public ResponseEntity<CategoryQuery> createCategory(@RequestBody CategoryCommand categoryCommand)
    {
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
