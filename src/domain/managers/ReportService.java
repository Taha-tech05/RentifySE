package domain.managers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repositories.ReportRepository;

public class ReportService {

	private final ReportRepository reportRepo = new ReportRepository();

	/**
	 * Returns report data as plain Java objects. Controller layer will convert
	 * Lists to ObservableList for JavaFX.
	 */
	public static ReportService getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private static class InstanceHolder {
		private static final ReportService INSTANCE = new ReportService();
	}

	public Map<String, Object> generateReport(String type, String period) {

		LocalDate today = LocalDate.now();
		LocalDate from = today;
		LocalDate to = today;

		// ---------- PERIOD LOGIC ----------
		switch (period.toLowerCase()) {
		case "daily":
			from = today;
			to = today;
			break;

		case "weekly":
			from = today.minusDays(6);
			to = today;
			break;

		case "monthly":
			from = today.withDayOfMonth(1);
			to = today;
			break;

		default:
			from = today;
			to = today;
		}

		Map<String, Object> result = new HashMap<>();
		result.put("type", type);
		result.put("period", period);
		result.put("from", from.toString());
		result.put("to", to.toString());

		// ---------- REPORT TYPES ----------
		switch (type.toLowerCase()) {

		case "revenue": {
			double revenue = reportRepo.getTotalRevenue(from, to);
			result.put("data", revenue);
			break;
		}

		case "bookings": {
			int bookings = reportRepo.getTotalBookings(from, to);
			result.put("data", bookings);
			break;
		}

		case "ownerrevenue": {
			List<Map<String, Object>> ownerData = reportRepo.getRevenueByOwner(from, to);
			result.put("data", ownerData);
			break;
		}

		case "mostrented": {
			List<Map<String, Object>> rented = reportRepo.getMostRentedProducts(10);
			result.put("data", rented);
			break;
		}

		case "monthlyrevenue": {
			List<Map<String, Object>> monthly = reportRepo.getMonthlyRevenue(today.getYear());
			result.put("data", monthly);
			break;
		}

		case "platformsummary": {
			Map<String, Object> summary = reportRepo.getPlatformSummary();
			result.put("data", summary);
			break;
		}

		default:
			result.put("error", "Invalid report type");
		}

		return result;
	}
}
