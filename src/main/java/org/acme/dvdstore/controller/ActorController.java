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
import org.acme.dvdstore.model.Actor;
import org.acme.dvdstore.service.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

import static org.acme.dvdstore.base.FilePattern.ACTOR;

@Component
@Slf4j
public class ActorController extends BaseController {
	@Autowired
	ActorService actorService;

	@Scheduled(cron = "0/15 * * * * *")
	private void poll() {
		final Path actorPath = getRoot().resolve(StructurePattern.ENTITIES.getName()).resolve(ACTOR.getSuffix());
		log.trace("Looking for {} related commands in {}.", ACTOR.getSuffix(), actorPath);

		try (final Stream<Path> actorFilesPath = Files.find(actorPath, 1,
															(path, basicFileAttributes) -> String.valueOf(path)
																								 .endsWith(
																									 ACTOR.getSuffix() +
																										 "." +
																										 fileFormat))) {
			final List<File> commandFiles = actorFilesPath.map(Path::toFile).collect(Collectors.toList());
			if (commandFiles.size() == 0) {
				log.trace("Found no commands.");
			} else {
				log.debug("Found {} command(s).", commandFiles.size());
				processCommands(commandFiles);
			}
		} catch (final IOException e) {
			log.error("Error while reading files in {}.", actorPath);
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
						final List<Actor> actors = createActor(reader);
						if (!CollectionUtils.isEmpty(actors)) {
							generateResponse(constructResponseFile(file, generatedId), actors);
						}
						break;
					case UPDATE:
						updateActor(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case DELETE:
						deleteActor(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case READ:
						final Actor actorFound = getActor(reader);
						generateResponse(constructResponseFile(file, generatedId), actorFound);
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

	private List<Actor> createActor(final Reader reader) {
		final Actor[] actors = gson.fromJson(reader, Actor[].class);
		if (!CollectionUtils.isEmpty(Arrays.asList(actors))) {
			final List<Actor> newActors = actorService.createAll(actors);
			log.debug("{} actor(s) loaded.", actors.length);
			return newActors;
		} else {
			log.debug("No actors loaded from file.");
			return Collections.emptyList();
		}
	}

	private void updateActor(final Reader reader) {
		final Actor actor = gson.fromJson(reader, Actor.class);
		if (actor != null) {
			actorService.update(actor);
			log.debug("1 actor updated.");
		} else {
			log.debug("No actors loaded from file.");
		}
	}

	private void deleteActor(final Reader reader) {
		final Actor actor = gson.fromJson(reader, Actor.class);
		if (actor != null) {
			actorService.delete(actor);
			log.debug("1 actor deleted.");
		} else {
			log.debug("No actors loaded from file.");
		}
	}

	private Actor getActor(final Reader reader) {
		final Long actorId = gson.fromJson(reader, Long.class);
		final Actor actorFound = actorService.get(actorId);
		log.debug("Actor ({}) found and retrieved.", actorId);
		return actorFound;
	}
}
