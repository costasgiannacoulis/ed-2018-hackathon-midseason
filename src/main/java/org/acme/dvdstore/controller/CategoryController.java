package org.acme.dvdstore.controller;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.acme.dvdstore.base.CommandPattern;
import org.acme.dvdstore.base.StructurePattern;
import org.acme.dvdstore.model.Category;
import org.acme.dvdstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

import static org.acme.dvdstore.base.FilePattern.CATEGORY;

@Component
@Slf4j
public class CategoryController extends BaseController {
	@Autowired
	CategoryService categoryService;

	@Scheduled(cron = "0/15 * * * * *")
	private void poll() {
		final Path categoryPath = Paths.get(root).resolve(StructurePattern.ENTITIES.getName()).resolve(
			CATEGORY.getSuffix());
		log.trace("Looking for {} related commands in {}.", CATEGORY.getSuffix(), categoryPath);

		try (final Stream<Path> categoryFilesPath = Files.find(categoryPath, 1,
															   (path, basicFileAttributes) -> String.valueOf(path)
																									.endsWith(CATEGORY
																												  .getSuffix() +
																												  "." +
																												  fileFormat))) {
			final List<File> commandFiles = categoryFilesPath.map(Path::toFile).collect(Collectors.toList());
			if (commandFiles.size() == 0) {
				log.trace("Found no commands.");
			} else {
				log.debug("Found {} command(s).", commandFiles.size());
				processCommands(commandFiles);
			}
		} catch (final IOException e) {
			log.error("Error while reading files in {}.", categoryPath);
			e.printStackTrace();
		}
	}

	private void processCommands(final List<File> commands) {
		for (final File file : commands) {
			log.trace("Processing create command in file {}.", file);

			final String generatedId = dateTimeFormatter.format(LocalDateTime.now());

			try (final Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
				final String command = file.getName().split(separator)[0];

				switch (Objects.requireNonNull(CommandPattern.get(command))) {
					case CREATE:
						final List<Category> categories = createCategory(reader);
						if (!CollectionUtils.isEmpty(categories)) {
							generateResponse(constructResponseFile(file, generatedId), categories);
						}
						break;
					case UPDATE:
						updateCategory(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case DELETE:
						deleteCategory(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case READ:
						final Category categoryFound = getCategory(reader);
						generateResponse(constructResponseFile(file, generatedId), categoryFound);
				}
				archiveCommand(file, generatedId);
			} catch (final NullPointerException ex) {
				log.warn("Command could not be recognised.");
				try {
					archiveCommand(file, generatedId);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			} catch (final JsonSyntaxException ex) {
				log.error("Corrupted file {}.", file);
			} catch (final IOException e) {
				log.error("File {} cannot be accessed.", file);
				e.printStackTrace();
			}
		}
	}

	private List<Category> createCategory(final Reader reader) {
		final Category[] categories = gson.fromJson(reader, Category[].class);
		if (!CollectionUtils.isEmpty(Arrays.asList(categories))) {
			final List<Category> newCategories = categoryService.createAll(categories);
			log.debug("{} category(s) loaded.", categories.length);
			return newCategories;
		} else {
			log.debug("No categories loaded from file.");
			return Collections.emptyList();
		}
	}

	private void updateCategory(final Reader reader) {
		final Category category = gson.fromJson(reader, Category.class);
		if (category != null) {
			categoryService.update(category);
			log.debug("1 category updated.");
		} else {
			log.debug("No categories loaded from file.");
		}
	}

	private void deleteCategory(final Reader reader) {
		final Category category = gson.fromJson(reader, Category.class);
		if (category != null) {
			categoryService.delete(category);
			log.debug("1 category deleted.");
		} else {
			log.debug("No categories loaded from file.");
		}
	}

	private Category getCategory(final Reader reader) {
		final Long categoryId = gson.fromJson(reader, Long.class);
		final Category categoryFound = categoryService.get(categoryId);
		log.debug("Category ({}) found and retrieved.", categoryId);
		return categoryFound;
	}
}
