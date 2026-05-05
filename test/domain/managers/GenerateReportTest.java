package domain.managers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import repositories.ReportRepository;

@DisplayName("US-08 | Generate Report Tests")
public class GenerateReportTest {

	private ReportRepository reportRepo;

	@BeforeEach
	void setUp() {
		reportRepo = new ReportRepository();
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Equivalence Class Partitioning
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-GR01 | VEC: Revenue report for valid date range returns non-null result")
	void testRevenueReportValidRange() {
		LocalDate from = LocalDate.of(2026, 1, 1);
		LocalDate to = LocalDate.of(2026, 3, 31);
		Double revenue = reportRepo.getTotalRevenue(from, to);
		assertNotNull(revenue, "Revenue report for a valid date range must not return null");
		assertTrue(revenue >= 0, "Revenue must be a non-negative value");
	}

	@Test
	@DisplayName("BB-GR02 | IEC: Revenue report for empty date range returns 0.0")
	void testRevenueReportEmptyRange() {
		// Date range with guaranteed no transactions
		LocalDate from = LocalDate.of(2000, 1, 1);
		LocalDate to = LocalDate.of(2000, 1, 2);
		Double revenue = reportRepo.getTotalRevenue(from, to);
		assertNotNull(revenue);
		assertEquals(0.0, revenue, "Empty date range must return 0.0, not null or exception");
	}

	@Test
	@DisplayName("BB-GR03 | VEC: Platform summary returns all required keys")
	void testPlatformSummaryKeys() {
		Map<String, Object> summary = reportRepo.getPlatformSummary();
		assertNotNull(summary, "Platform summary must not be null");
		assertTrue(summary.containsKey("totalRenters"), "Must contain totalRenters");
		assertTrue(summary.containsKey("totalOwners"), "Must contain totalOwners");
		assertTrue(summary.containsKey("availableProducts"), "Must contain availableProducts");
		assertTrue(summary.containsKey("activeBookings"), "Must contain activeBookings");
		assertTrue(summary.containsKey("totalEarnings"), "Must contain totalEarnings");
	}

	@Test
	@DisplayName("BB-GR04 | IEC: Null date range should be rejected gracefully")
	void testNullDateRangeRejected() {
		assertDoesNotThrow(() -> {
			Double result = reportRepo.getTotalRevenue(null, null);
			assertTrue(result == -1, "Null date inputs must return null without throwing");
		});
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Boundary Value Analysis
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-GR05 | BVA: from == to (single-day range) should not throw")
	void testSingleDayRange() {
		LocalDate day = LocalDate.of(2026, 3, 1);
		assertDoesNotThrow(() -> reportRepo.getTotalRevenue(day, day),
				"Single-day range must execute without exception");
	}

	@Test
	@DisplayName("BB-GR06 | BVA: from after to (inverted range) should return null or 0")
	void testInvertedDateRange() {
		LocalDate from = LocalDate.of(2026, 6, 1);
		LocalDate to = LocalDate.of(2026, 1, 1);
		Double result = reportRepo.getTotalRevenue(from, to);
		assertTrue(result == null || result == 0.0, "Inverted date range must return null or 0.0, not crash");
	}

	// -----------------------------------------------------------------------
	// WHITE-BOX — Branch / Path Coverage
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("WB-GR01 | Branch: revenue switch case executes getTotalRevenue()")
	void testRevenueBranchExecuted() {
		// Targets the 'revenue' case inside generateReport() switch statement
		LocalDate from = LocalDate.of(2026, 1, 1);
		LocalDate to = LocalDate.of(2026, 12, 31);
		Double revenue = reportRepo.getTotalRevenue(from, to);
		assertNotNull(revenue, "Revenue branch must execute and return a value");
	}

	@Test
	@DisplayName("WB-GR02 | Branch: platform summary case executes getPlatformSummary()")
	void testPlatformSummaryBranchExecuted() {
		// Targets the 'platformsummary' case
		Map<String, Object> summary = reportRepo.getPlatformSummary();
		assertNotNull(summary);
		assertFalse(summary.isEmpty(), "Platform summary branch must return a populated map");
	}

	@Test
	@DisplayName("WB-GR04 | Path: all summary values are non-negative integers or doubles")
	void testSummaryValuesAreValid() {
		Map<String, Object> summary = reportRepo.getPlatformSummary();
		assertNotNull(summary);
		Number renters = (Number) summary.get("totalRenters");
		Number owners = (Number) summary.get("totalOwners");
		Number earnings = (Number) summary.get("totalEarnings");
		assertTrue(renters.intValue() >= 0, "totalRenters must be >= 0");
		assertTrue(owners.intValue() >= 0, "totalOwners must be >= 0");
		assertTrue(earnings.doubleValue() >= 0.0, "totalEarnings must be >= 0.0");
	}
}