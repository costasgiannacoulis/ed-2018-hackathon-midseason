package org.acme.dvdstore.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.acme.dvdstore.base.StructurePattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseController {
	@Autowired
	Gson gson;
	@Value("${dvdstore.file.format}")
	protected String fileFormat;
	@Value("${dvdstore.directory.root}")
	protected String root;
	@Value("${dvdstore.file.command.separator}")
	protected String separator;
	@Value("${dvdstore.file.response.suffix}")
	protected String responseSuffix;

	protected DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private Path archivePath;

	@PostConstruct
	protected void init() {
		archivePath = Objects.requireNonNull(Paths.get(root).resolve(StructurePattern.ARCHIVE.getName()));
	}

	protected Path getRoot() {
		if (StringUtils.isEmpty(root)) {
			root = System.getProperty("user.home");
		}
		return Paths.get(root);
	}

	protected void archiveCommand(final File file, final String generatedId) throws IOException {
		Files.move(file.toPath(), archivePath.resolve(constructArchiveFile(file, generatedId)),
				   StandardCopyOption.REPLACE_EXISTING);
	}

	protected String constructArchiveFile(final File file, final String generatedId) {
		return file.getName().substring(0, file.getName().indexOf(".")) + separator + generatedId + "." + fileFormat;
	}

	protected String constructResponseFile(final File file, final String generatedId) {
		return file.getName().substring(0, file.getName().indexOf(".")) + separator + generatedId + separator +
			responseSuffix + "." + fileFormat;
	}

	protected void generateResponse(final String responseFile, final Object content) {
		try (final FileOutputStream fos = new FileOutputStream(getRoot().resolve(StructurePattern.RESPONSES.getName())
																		.resolve(responseFile).toFile());
			 final OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			gson.toJson(content, osr);
		} catch (final IOException e) {
			log.debug("Unable to generate response.");
			e.printStackTrace();
		}
	}
}
