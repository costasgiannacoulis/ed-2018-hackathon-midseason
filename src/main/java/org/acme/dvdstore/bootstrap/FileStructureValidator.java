package org.acme.dvdstore.bootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.acme.dvdstore.base.DataAction;
import org.acme.dvdstore.base.FilePattern;
import org.acme.dvdstore.base.StructurePattern;
import org.acme.dvdstore.model.Actor;
import org.acme.dvdstore.model.Category;
import org.acme.dvdstore.model.Customer;
import org.acme.dvdstore.model.Film;
import org.acme.dvdstore.model.Language;
import org.acme.dvdstore.model.Rental;
import org.acme.dvdstore.service.ActorService;
import org.acme.dvdstore.service.CategoryService;
import org.acme.dvdstore.service.CustomerService;
import org.acme.dvdstore.service.FilmService;
import org.acme.dvdstore.service.LanguageService;
import org.acme.dvdstore.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

import static java.nio.file.Files.createDirectory;
import static org.acme.dvdstore.base.DataAction.BACKUP;
import static org.acme.dvdstore.base.DataAction.LOAD;
import static org.acme.dvdstore.base.FilePattern.ACTOR;
import static org.acme.dvdstore.base.FilePattern.CATEGORY;
import static org.acme.dvdstore.base.FilePattern.CUSTOMER;
import static org.acme.dvdstore.base.FilePattern.FILM;
import static org.acme.dvdstore.base.FilePattern.LANGUAGE;
import static org.acme.dvdstore.base.FilePattern.RENTAL;

@Component
@Slf4j
public class FileStructureValidator implements CommandLineRunner {
	@Autowired
	ActorService actorService;
	@Autowired
	CategoryService categoryService;
	@Autowired
	CustomerService customerService;
	@Autowired
	FilmService filmService;
	@Autowired
	LanguageService languageService;
	@Autowired
	RentalService rentalService;
	@Autowired
	Gson gson;
	@Value("${dvdstore.directory.root}")
	private String root;
	@Value("${dvdstore.file.format}")
	private String fileFormat;

	@Override
	public void run(final String... args) throws Exception {
		validateStructure();
		loadBackedUpData();
	}

	private void validateStructure() {
		//Create root
		createStructure(getRoot());

		// Create 1st level directories
		EnumSet.allOf(StructurePattern.class).forEach(t -> createStructure(getRoot().resolve(t.getName())));

		// Create 2nd level directories under entities
		final Path parentPath = getRoot().resolve(StructurePattern.ENTITIES.getName());
		EnumSet.allOf(FilePattern.class).forEach(t -> createStructure(parentPath.resolve(t.getSuffix())));
	}

	private Path getRoot() {
		if (StringUtils.isEmpty(root)) {
			root = System.getProperty("user.home");
		}
		return Paths.get(root);
	}

	private Path getFullPath(final FilePattern filePattern) {
		return Paths.get(getRoot().resolve(StructurePattern.BACKUP.getName())
								  .resolve(filePattern.getDumpName() + "." + fileFormat).toString());
	}

	private void createStructure(final Path path) {
		if (!Files.exists(path)) {
			try {
				createDirectory(path);
			} catch (final IOException e) {
				e.printStackTrace();
			}
			log.debug("Created directory '{}'", path);
		} else {
			log.debug("Found directory '{}'", path);
		}
	}

	private void loadBackedUpData() throws IOException {
		final Path backupPath = getRoot().resolve(StructurePattern.BACKUP.getName());
		final Stream<Path> backupFilesPath = Files.find(backupPath, 1,
														(path, basicFileAttributes) -> String.valueOf(path)
																							 .endsWith(fileFormat));
		final List<File> backupFiles = backupFilesPath.map(path -> path.toFile()).collect(Collectors.toList());
		log.debug("Initiate load data procedure for {} backup file(s).", backupFiles.size());
		log.debug("---------------------------------------------------");

		EnumSet.allOf(FilePattern.class).forEach(t -> {
			if (Files.exists(getFullPath(t), LinkOption.NOFOLLOW_LINKS)) {
				log.debug("Found dump {}.", getFullPath(t));
				loadData(t);
			} else {
				log.debug("Skipped dump {}.", getFullPath(t));
			}
		});
		log.debug("---------------------------------------------------");
		log.debug("Completed load data procedure.");
	}

	private void loadData(final FilePattern filePattern) {
		processData(filePattern, LOAD);
	}

	private void saveData(final FilePattern filePattern) {
		processData(filePattern, BACKUP);
	}

	private void processData(final FilePattern filePattern, final DataAction action) {
		switch (filePattern) {
			case ACTOR:
				if (LOAD == action) {
					loadActors();
				} else {
					saveActors();
				}
				break;
			case CATEGORY:
				if (LOAD == action) {
					loadCategories();
				} else {
					saveCategories();
				}
				break;
			case CUSTOMER:
				if (LOAD == action) {
					loadCustomers();
				} else {
					saveCustomers();
				}
				break;
			case FILM:
				if (LOAD == action) {
					loadFilms();
				} else {
					saveFilms();
				}
				break;
			case LANGUAGE:
				if (LOAD == action) {
					loadLanguages();
				} else {
					saveLanguages();
				}
				break;
			case RENTAL:
				if (LOAD == action) {
					loadRentals();
				} else {
					saveRentals();
				}
		}
	}

	private void loadActors() {
		final Path path = getFullPath(ACTOR);
		try (final Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			final Actor[] actors = gson.fromJson(reader, Actor[].class);
			if (!CollectionUtils.isEmpty(Arrays.asList(actors))) {
				actorService.createAll(actors);
				log.debug("{} actor(s) loaded.", actorService.findAll().size());
			} else {
				log.debug("No actors loaded.");
			}
		} catch (final JsonSyntaxException ex) {
			log.error("Corrupted backup file {}.", path);
		} catch (final IOException e) {
			log.error("File {} cannot be accessed.", path);
			e.printStackTrace();
		}
	}

	private void loadCategories() {
		final Path path = getFullPath(CATEGORY);
		try (final Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			final Category[] categories = gson.fromJson(reader, Category[].class);
			if (!CollectionUtils.isEmpty(Arrays.asList(categories))) {
				categoryService.createAll(categories);
				log.debug("{} categories loaded.", categoryService.findAll().size());
			} else {
				log.debug("No categories loaded.");
			}
		} catch (final JsonSyntaxException ex) {
			log.error("Corrupted backup file {}.", path);
		} catch (final IOException e) {
			log.error("File {} cannot be accessed.", path);
			e.printStackTrace();
		}
	}

	private void loadCustomers() {
		final Path path = getFullPath(CUSTOMER);
		try (final Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			final Customer[] customers = gson.fromJson(reader, Customer[].class);
			if (customers != null && !CollectionUtils.isEmpty(Arrays.asList(customers))) {
				customerService.createAll(customers);
				log.debug("{} customers loaded.", customerService.findAll().size());
			} else {
				log.debug("No customers loaded.");
			}
		} catch (final JsonSyntaxException ex) {
			log.error("Corrupted backup file {}.", path);
		} catch (final IOException e) {
			log.error("File {} cannot be accessed.", path);
			e.printStackTrace();
		}
	}

	private void loadFilms() {
		final Path path = getFullPath(FILM);
		try (final Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			final Film[] films = gson.fromJson(reader, Film[].class);
			if (!CollectionUtils.isEmpty(Arrays.asList(films))) {
				filmService.createAll(films);
				log.debug("{} film(s) loaded.", filmService.findAll().size());
			} else {
				log.debug("No films loaded.");
			}
		} catch (final JsonSyntaxException ex) {
			log.error("Corrupted backup file {}.", path);
		} catch (final IOException e) {
			log.error("File {} cannot be accessed.", path);
			e.printStackTrace();
		}
	}

	private void loadLanguages() {
		final Path path = getFullPath(LANGUAGE);
		try (final Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			final Language[] languages = gson.fromJson(reader, Language[].class);
			if (!CollectionUtils.isEmpty(Arrays.asList(languages))) {
				languageService.createAll(languages);
				log.debug("{} language(s) loaded.", languageService.findAll().size());
			} else {
				log.debug("No languages loaded.");
			}
		} catch (final JsonSyntaxException ex) {
			log.error("Corrupted backup file {}.", path);
		} catch (final IOException e) {
			log.error("File {} cannot be accessed.", path);
			e.printStackTrace();
		}
	}

	private void loadRentals() {
		final Path path = getFullPath(RENTAL);
		try (final Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			final Rental[] rentals = gson.fromJson(reader, Rental[].class);
			if (rentals != null && !CollectionUtils.isEmpty(Arrays.asList(rentals))) {
				rentalService.createAll(rentals);
				log.debug("{} rental(s) loaded.", rentalService.findAll().size());
			} else {
				log.debug("No rentals loaded.");
			}
		} catch (final JsonSyntaxException ex) {
			log.error("Corrupted backup file {}.", path);
		} catch (final IOException e) {
			log.error("File {} cannot be accessed.", path);
			e.printStackTrace();
		}
	}

	@PreDestroy
	public void backupData() {
		log.debug("Initiate backup procedure.");
		log.debug("---------------------------------------------------");

		EnumSet.allOf(FilePattern.class).forEach(t -> saveData(t));
		log.debug("---------------------------------------------------");
		log.debug("Completed backup procedure.");
	}

	private void saveActors() {
		final List<Actor> actors = actorService.findAll();
		try (final FileOutputStream fos = new FileOutputStream(getFullPath(ACTOR).toFile());
			 final OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			gson.toJson(actors, osr);
			log.debug("Backed up {} actor(s) in {}.", actors.size(), getFullPath(ACTOR));
		} catch (final IOException e) {
			log.debug("Unable to back up actors.");
			e.printStackTrace();
		}
	}

	private void saveCategories() {
		final List<Category> categories = categoryService.findAll();
		try (final FileOutputStream fos = new FileOutputStream(getFullPath(CATEGORY).toFile());
			 final OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			gson.toJson(categories, osr);
			log.debug("Backed up {} categories in {}.", categories.size(), getFullPath(CATEGORY));
		} catch (final IOException e) {
			log.debug("Unable to back up categories.");
			e.printStackTrace();
		}
	}

	private void saveCustomers() {
		final List<Customer> customers = customerService.findAll();
		try (final FileOutputStream fos = new FileOutputStream(getFullPath(CUSTOMER).toFile());
			 final OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			gson.toJson(customers, osr);
			log.debug("Backed up {} customer(s) in {}.", customers.size(), getFullPath(CUSTOMER));
		} catch (final IOException e) {
			log.debug("Unable to back up customers.");
			e.printStackTrace();
		}
	}

	private void saveFilms() {
		final List<Film> films = filmService.findAll();
		try (final FileOutputStream fos = new FileOutputStream(getFullPath(FILM).toFile());
			 final OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			gson.toJson(films, osr);
			log.debug("Backed up {} film(s) in {}.", films.size(), getFullPath(FILM));
		} catch (final IOException e) {
			log.debug("Unable to back up films.");
			e.printStackTrace();
		}
	}

	private void saveLanguages() {
		final List<Language> languages = languageService.findAll();
		try (final FileOutputStream fos = new FileOutputStream(getFullPath(LANGUAGE).toFile());
			 final OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			gson.toJson(languages, osr);
			log.debug("Backed up {} language(s) in {}.", languages.size(), getFullPath(LANGUAGE));
		} catch (final IOException e) {
			log.debug("Unable to back up languages.");
			e.printStackTrace();
		}
	}

	private void saveRentals() {
		final List<Rental> rentals = rentalService.findAll();
		try (final FileOutputStream fos = new FileOutputStream(getFullPath(RENTAL).toFile());
			 final OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			gson.toJson(rentals, osr);
			log.debug("Backed up {} rental(s) in {}.", rentals.size(), getFullPath(RENTAL));
		} catch (final IOException e) {
			log.debug("Unable to back up rentals.");
			e.printStackTrace();
		}
	}
}
