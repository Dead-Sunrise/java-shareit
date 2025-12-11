package ru.practicum.shareit.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.validator.BookingValidator;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;
	private final BookingValidator bookingValidator;

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
								 @RequestBody BookingDto bookingDto) {
		bookingValidator.bookingValidate(bookingDto);
		return bookingClient.create(userId, bookingDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> processingBookingResponse(@RequestHeader("X-Sharer-User-Id") Long ownerId,
													@PathVariable Long bookingId,
													@RequestParam Boolean approved) {
		bookingValidator.approvedValidate(approved);
		return bookingClient.processingBookingResponse(ownerId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> findBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
										  @PathVariable Long bookingId) {
		return bookingClient.findBookingById(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> findAllBookingsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
													  @RequestParam(defaultValue = "ALL", required = false) BookingState state) {
		return bookingClient.findAllBookingsByUser(userId, state);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> findAllBookingsByUserItems(@RequestHeader("X-Sharer-User-Id") Long userId,
														   @RequestParam(defaultValue = "ALL", required = false) BookingState state) {
		return bookingClient.findAllBookingsByUserItems(userId, state);
	}
}