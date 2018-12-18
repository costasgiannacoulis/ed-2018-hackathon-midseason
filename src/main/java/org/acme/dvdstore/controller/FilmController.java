package org.acme.dvdstore.controller;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.acme.dvdstore.base.CommandPattern;
import org.acme.dvdstore.base.StructurePattern;
import org.acme.dvdstore.model.Film;
import org.acme.dvdstore.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

import static org.acme.dvdstore.base.FilePattern.FILM;

@Component
@Slf4j
public class FilmController extends BaseController {
	@Autowired
	FilmService filmService;

	@Scheduled(cron = "0/15 * * * * *")
	private void poll() {
		final Path filmPath = getRoot().resolve(StructurePattern.ENTITIES.getName()).resolve(FILM.getSuffix());
		log.trace("Looking for {} related commands in {}.", FILM.getSuffix(), filmPath);

		try (final Stream<Path> filmFilesPath = Files.find(filmPath, 1,
														   (path, basicFileAttributes) -> String.valueOf(path).endsWith(
															   FILM.getSuffix() + "." + fileFormat))) {
			final List<File> commandFiles = filmFilesPath.map(Path::toFile).collect(Collectors.toList());
			if (commandFiles.size() == 0) {
				log.trace("Found no commands.");
			} else {
				log.debug("Found {} command(s).", commandFiles.size());
				processCommands(commandFiles);
			}
		} catch (final IOException e) {
			log.error("Error while reading files in {}.", filmPath);
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
						final List<Film> films = createFilm(reader);
						if (!CollectionUtils.isEmpty(films)) {
							generateResponse(constructResponseFile(file, generatedId), films);
						}
						break;
					case UPDATE:
						updateFilm(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case DELETE:
						deleteFilm(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case READ:
						final Film filmFound = getFilm(reader);
						generateResponse(constructResponseFile(file, generatedId), filmFound);
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

	private List<Film> createFilm(final Reader reader) {
		final Film[] films = gson.fromJson(reader, Film[].class);
		if (!CollectionUtils.isEmpty(Arrays.asList(films))) {
			final List<Film> newFilms = filmService.createAll(films);
			log.debug("{} film(s) loaded.", films.length);
			return newFilms;
		} else {
			log.debug("No films loaded from file.");
			return Collections.emptyList();
		}
	}

	private void updateFilm(final Reader reader) {
		final Film film = gson.fromJson(reader, Film.class);
		if (film != null) {
			filmService.update(film);
			log.debug("1 film updated.");
		} else {
			log.debug("No films loaded from file.");
		}
	}

	private void deleteFilm(final Reader reader) {
		final Film film = gson.fromJson(reader, Film.class);
		if (film != null) {
			filmService.delete(film);
			log.debug("1 film deleted.");
		} else {
			log.debug("No films loaded from file.");
		}
	}

	private Film getFilm(final Reader reader) {
		final Long filmId = gson.fromJson(reader, Long.class);
		final Film filmFound = filmService.get(filmId);
		log.debug("Film ({}) found and retrieved.", filmId);
		return filmFound;
	}
}
