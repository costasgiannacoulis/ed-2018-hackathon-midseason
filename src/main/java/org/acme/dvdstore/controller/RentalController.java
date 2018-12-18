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
import org.acme.dvdstore.model.Rental;
import org.acme.dvdstore.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

import static org.acme.dvdstore.base.FilePattern.RENTAL;

@Component
@Slf4j
public class RentalController extends BaseController {
	@Autowired
	RentalService rentalService;

	@Scheduled(cron = "0/15 * * * * *")
	private void poll() {
		final Path rentalPath = getRoot().resolve(StructurePattern.ENTITIES.getName()).resolve(
			RENTAL.getSuffix());
		log.trace("Looking for {} related commands in {}.", RENTAL.getSuffix(), rentalPath);

		try (final Stream<Path> rentalFilesPath = Files.find(rentalPath, 1,
															 (path, basicFileAttributes) -> String.valueOf(path)
																								  .endsWith(RENTAL
																												.getSuffix() +
																												"." +
																												fileFormat))) {
			final List<File> commandFiles = rentalFilesPath.map(Path::toFile).collect(Collectors.toList());
			if (commandFiles.size() == 0) {
				log.trace("Found no commands.");
			} else {
				log.debug("Found {} command(s).", commandFiles.size());
				processCommands(commandFiles);
			}
		} catch (final IOException e) {
			log.error("Error while reading files in {}.", rentalPath);
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
						final List<Rental> rentals = createRental(reader);
						if (!CollectionUtils.isEmpty(rentals)) {
							generateResponse(constructResponseFile(file, generatedId), rentals);
						}
						break;
					case UPDATE:
						updateRental(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case DELETE:
						deleteRental(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case READ:
						final Rental rentalFound = getRental(reader);
						generateResponse(constructResponseFile(file, generatedId), rentalFound);
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

	private List<Rental> createRental(final Reader reader) {
		final Rental[] rentals = gson.fromJson(reader, Rental[].class);
		if (!CollectionUtils.isEmpty(Arrays.asList(rentals))) {
			final List<Rental> newRentals = rentalService.createAll(rentals);
			log.debug("{} rental(s) loaded.", rentals.length);
			return newRentals;
		} else {
			log.debug("No rentals loaded from file.");
			return Collections.emptyList();
		}
	}

	private void updateRental(final Reader reader) {
		final Rental rental = gson.fromJson(reader, Rental.class);
		if (rental != null) {
			rentalService.update(rental);
			log.debug("1 rental updated.");
		} else {
			log.debug("No rentals loaded from file.");
		}
	}

	private void deleteRental(final Reader reader) {
		final Rental rental = gson.fromJson(reader, Rental.class);
		if (rental != null) {
			rentalService.delete(rental);
			log.debug("1 rental deleted.");
		} else {
			log.debug("No rentals loaded from file.");
		}
	}

	private Rental getRental(final Reader reader) {
		final Long rentalId = gson.fromJson(reader, Long.class);
		final Rental rentalFound = rentalService.get(rentalId);
		log.debug("Rental ({}) found and retrieved.", rentalId);
		return rentalFound;
	}
}
