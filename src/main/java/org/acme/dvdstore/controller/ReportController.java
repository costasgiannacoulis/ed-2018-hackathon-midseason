package org.acme.dvdstore.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.acme.dvdstore.base.Report;
import org.acme.dvdstore.base.StructurePattern;
import org.acme.dvdstore.model.Film;
import org.acme.dvdstore.model.Rental;
import org.acme.dvdstore.service.RentalService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ReportController extends BaseController {
	@Value("${dvdstore.file.report.suffix}")
	protected String reportSuffix;
	@Value("${dvdstore.file.report.format}")
	protected String reportFormat;

	@Autowired
	RentalService rentalService;

	@Scheduled(cron = "0/15 * * * * *")
	private void poll() {
		final Path reportPath = getRoot().resolve(StructurePattern.REPORTS.getName());
		log.trace("Looking for report related commands in {}.", reportPath);

		try (final Stream<Path> actorFilesPath = Files.find(reportPath, 1,
															(path, basicFileAttributes) -> String.valueOf(path)
																								 .endsWith(
																									 reportSuffix +
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
			log.error("Error while reading files in {}.", reportPath);
			e.printStackTrace();
		}
	}

	private void processCommands(final List<File> commands) {
		for (final File file : commands) {
			log.trace("Processing create command in file {}.", file);

			final String generatedId = dateTimeFormatter.format(LocalDateTime.now());

			try (final Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
				final String command = file.getName().split(separator)[0];

				switch (Objects.requireNonNull(Report.get(command))) {
					case DVD_RENTAL:
						final Map<Film, Integer> report = dvdRentalList(reader);
						if (!CollectionUtils.isEmpty(report)) {
							generateReport(constructReportFile(file, generatedId), report);
						}
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

	private Map<Film, Integer> dvdRentalList(final Reader reader) {
		final List<Rental> rentals = rentalService.findAll();
		final Map<Film, Integer> rentalOccurrences = new HashMap<>();

		for (final Rental rental : rentals) {
			if ((rentalOccurrences.get(rental.getFilm()) == null)) {
				rentalOccurrences.put(rental.getFilm(), 1);
			} else {
				rentalOccurrences.put(rental.getFilm(), rentalOccurrences.get(rental.getFilm()) + 1);
			}
		}
		return rentalOccurrences;
	}

	protected String constructReportFile(final File file, final String generatedId) {
		return file.getName().substring(0, file.getName().indexOf(".")) + separator + generatedId + "." + reportFormat;
	}

	protected void generateReport(final String reportFile, final Map<Film, Integer> report) {
		final Workbook workbook = new XSSFWorkbook();

		// Create a Sheet
		final Sheet sheet = workbook.createSheet("DVD Rentals");

		// Create a Font for styling header cells
		final Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setColor(IndexedColors.RED.getIndex());

		// Create a CellStyle with the font
		final CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		// Create a Row
		final Row headerRow = sheet.createRow(0);

		Cell cell = headerRow.createCell(0);
		cell.setCellValue("Film Title");
		cell.setCellStyle(headerCellStyle);
		cell = headerRow.createCell(1);
		cell.setCellValue("Rentals");
		cell.setCellStyle(headerCellStyle);

		// Create Other rows and cells with employees data
		int rowNum = 1;
		for (final Film film : report.keySet()) {
			final Row row = sheet.createRow(rowNum++);

			row.createCell(0).setCellValue(film.getTitle());
			row.createCell(1).setCellValue(report.get(film));
		}

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);

		try (final FileOutputStream fos = new FileOutputStream(getRoot().resolve(StructurePattern.RESPONSES.getName())
																		.resolve(reportFile).toFile())) {
			workbook.write(fos);
			workbook.close();
		} catch (final IOException e) {
			log.debug("Unable to generate report.");
			e.printStackTrace();
		}
	}
}
