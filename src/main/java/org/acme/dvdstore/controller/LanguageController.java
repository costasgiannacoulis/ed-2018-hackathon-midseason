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
import org.acme.dvdstore.model.Language;
import org.acme.dvdstore.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

import static org.acme.dvdstore.base.FilePattern.LANGUAGE;

@Component
@Slf4j
public class LanguageController extends BaseController {
	@Autowired
	LanguageService languageService;

	@Scheduled(cron = "0/15 * * * * *")
	private void poll() {
		final Path languagePath = Paths.get(root).resolve(StructurePattern.ENTITIES.getName()).resolve(
			LANGUAGE.getSuffix());
		log.trace("Looking for {} related commands in {}.", LANGUAGE.getSuffix(), languagePath);

		try (final Stream<Path> languageFilesPath = Files.find(languagePath, 1,
															   (path, basicFileAttributes) -> String.valueOf(path)
																									.endsWith(LANGUAGE
																												  .getSuffix() +
																												  "." +
																												  fileFormat))) {
			final List<File> commandFiles = languageFilesPath.map(Path::toFile).collect(Collectors.toList());
			if (commandFiles.size() == 0) {
				log.trace("Found no commands.");
			} else {
				log.debug("Found {} command(s).", commandFiles.size());
				processCommands(commandFiles);
			}
		} catch (final IOException e) {
			log.error("Error while reading files in {}.", languagePath);
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
						final List<Language> languages = createLanguage(reader);
						if (!CollectionUtils.isEmpty(languages)) {
							generateResponse(constructResponseFile(file, generatedId), languages);
						}
						break;
					case UPDATE:
						updateLanguage(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case DELETE:
						deleteLanguage(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case READ:
						final Language languageFound = getLanguage(reader);
						generateResponse(constructResponseFile(file, generatedId), languageFound);
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

	private List<Language> createLanguage(final Reader reader) {
		final Language[] languages = gson.fromJson(reader, Language[].class);
		if (!CollectionUtils.isEmpty(Arrays.asList(languages))) {
			final List<Language> newLanguages = languageService.createAll(languages);
			log.debug("{} language(s) loaded.", languages.length);
			return newLanguages;
		} else {
			log.debug("No languages loaded from file.");
			return Collections.emptyList();
		}
	}

	private void updateLanguage(final Reader reader) {
		final Language language = gson.fromJson(reader, Language.class);
		if (language != null) {
			languageService.update(language);
			log.debug("1 language updated.");
		} else {
			log.debug("No languages loaded from file.");
		}
	}

	private void deleteLanguage(final Reader reader) {
		final Language language = gson.fromJson(reader, Language.class);
		if (language != null) {
			languageService.delete(language);
			log.debug("1 language deleted.");
		} else {
			log.debug("No languages loaded from file.");
		}
	}

	private Language getLanguage(final Reader reader) {
		final Long languageId = gson.fromJson(reader, Long.class);
		final Language languageFound = languageService.get(languageId);
		log.debug("Language ({}) found and retrieved.", languageId);
		return languageFound;
	}
}
