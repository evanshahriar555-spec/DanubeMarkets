package hu.danube;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/categories")
public class CategoryApi {
  final CategoryRepository repo;

  public CategoryApi(CategoryRepository r) {
    repo = r;
  }

  @GetMapping
  public List<Category> all() {
    return repo.findAll(org.springframework.data.domain.Sort.by("cname"));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Category create(@Valid @RequestBody Category c) {
    if (repo.existsById(c.cname))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
    return repo.save(c);
  }

  @PutMapping("/{name}")
  public Category update(@PathVariable String name, @Valid @RequestBody Category c) {
    if (!repo.existsById(name)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    if (!name.equals(c.cname))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Name is the primary key and cannot be changed");
    return repo.save(c);
  }

  @DeleteMapping("/{name}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String name) {
    if (!repo.existsById(name)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    repo.deleteById(name);
  }
}
