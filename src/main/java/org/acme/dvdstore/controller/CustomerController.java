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
import org.acme.dvdstore.model.Customer;
import org.acme.dvdstore.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

import static org.acme.dvdstore.base.FilePattern.CUSTOMER;

@Component
@Slf4j
public class CustomerController extends BaseController {
	@Autowired
	CustomerService customerService;

	@Scheduled(cron = "0/15 * * * * *")
	private void poll() {
		final Path customerPath = getRoot().resolve(StructurePattern.ENTITIES.getName()).resolve(
			CUSTOMER.getSuffix());
		log.trace("Looking for {} related commands in {}.", CUSTOMER.getSuffix(), customerPath);

		try (final Stream<Path> customerFilesPath = Files.find(customerPath, 1,
															   (path, basicFileAttributes) -> String.valueOf(path)
																									.endsWith(CUSTOMER
																												  .getSuffix() +
																												  "." +
																												  fileFormat))) {
			final List<File> commandFiles = customerFilesPath.map(Path::toFile).collect(Collectors.toList());
			if (commandFiles.size() == 0) {
				log.trace("Found no commands.");
			} else {
				log.debug("Found {} command(s).", commandFiles.size());
				processCommands(commandFiles);
			}
		} catch (final IOException e) {
			log.error("Error while reading files in {}.", customerPath);
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
						final List<Customer> customers = createCustomer(reader);
						if (!CollectionUtils.isEmpty(customers)) {
							generateResponse(constructResponseFile(file, generatedId), customers);
						}
						break;
					case UPDATE:
						updateCustomer(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case DELETE:
						deleteCustomer(reader);
						generateResponse(constructResponseFile(file, generatedId), "{status:\"OK\"}");
						break;
					case READ:
						final Customer customerFound = getCustomer(reader);
						generateResponse(constructResponseFile(file, generatedId), customerFound);
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

	private List<Customer> createCustomer(final Reader reader) {
		final Customer[] customers = gson.fromJson(reader, Customer[].class);
		if (!CollectionUtils.isEmpty(Arrays.asList(customers))) {
			final List<Customer> newCustomers = customerService.createAll(customers);
			log.debug("{} customer(s) loaded.", customers.length);
			return newCustomers;
		} else {
			log.debug("No customers loaded from file.");
			return Collections.emptyList();
		}
	}

	private void updateCustomer(final Reader reader) {
		final Customer customer = gson.fromJson(reader, Customer.class);
		if (customer != null) {
			customerService.update(customer);
			log.debug("1 customer updated.");
		} else {
			log.debug("No customers loaded from file.");
		}
	}

	private void deleteCustomer(final Reader reader) {
		final Customer customer = gson.fromJson(reader, Customer.class);
		if (customer != null) {
			customerService.delete(customer);
			log.debug("1 customer deleted.");
		} else {
			log.debug("No customers loaded from file.");
		}
	}

	private Customer getCustomer(final Reader reader) {
		final Long customerId = gson.fromJson(reader, Long.class);
		final Customer customerFound = customerService.get(customerId);
		log.debug("Customer ({}) found and retrieved.", customerId);
		return customerFound;
	}
}
