package mca.fincorebanking.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public final class NumericConfigStore {

    // Use JsonMapper.builder() for Jackson 3.x
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .build();

    private static final Path EXTERNAL_CONFIG_PATH = Path.of("config", "numeric-values.json");
    private static final Path SOURCE_CONFIG_PATH = Path.of("src", "main", "resources", "config", "numeric-values.json");
    private static AppNumericConfig cachedConfig;
    private static long cachedLastModified = Long.MIN_VALUE;

    private NumericConfigStore() {
    }

    public static synchronized AppNumericConfig get() {
        Path path = resolveWritableConfigPath();
        try {
            long lastModified = Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : Long.MIN_VALUE;
            if (cachedConfig == null || cachedLastModified != lastModified) {
                cachedConfig = readConfig(path);
                cachedLastModified = lastModified;
            }
            return cachedConfig;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read numeric config from " + path.toAbsolutePath(), ex);
        }
    }

    public static synchronized void save(AppNumericConfig config) {
        Path path = resolveWritableConfigPath();
        try {
            Files.createDirectories(path.getParent());
            // Pretty printing in Jackson 3.x
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), config);
            cachedConfig = config;
            cachedLastModified = Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write numeric config to " + path.toAbsolutePath(), ex);
        }
    }

    private static AppNumericConfig readConfig(Path path) throws IOException {
        if (Files.exists(path)) {
            return OBJECT_MAPPER.readValue(path.toFile(), AppNumericConfig.class);
        }

        try (InputStream stream = NumericConfigStore.class.getResourceAsStream("/config/numeric-values.json")) {
            if (stream != null) {
                return OBJECT_MAPPER.readValue(stream, AppNumericConfig.class);
            }
        }

        return new AppNumericConfig();
    }

    private static Path resolveWritableConfigPath() {
        if (Files.exists(EXTERNAL_CONFIG_PATH)) {
            return EXTERNAL_CONFIG_PATH;
        }
        return SOURCE_CONFIG_PATH;
    }

    // --- Nested config classes remain unchanged ---
    public static class AppNumericConfig {
        public Account account = new Account();
        public Security security = new Security();
        public Transactions transactions = new Transactions();
        public Interest interest = new Interest();
        public FixedDeposit fixedDeposit = new FixedDeposit();
        public Loan loan = new Loan();
        public Dashboard dashboard = new Dashboard();
        public ManagerReports managerReports = new ManagerReports();
        public DebitCard debitCard = new DebitCard();
        public SystemStats systemStats = new SystemStats();
        public Pdf pdf = new Pdf();
    }

    public static class Account {
        public int defaultInterestMonths = 12;
        public int accountNumberUuidEndIndex = 8;
        public double defaultBalance = 0.0;
    }

    public static class Security {
        public int maxFailedAttempts = 3;
        public int resetFailedAttempts = 0;
        public int failedAttemptIncrement = 1;
    }

    public static class Transactions {
        public int defaultPage = 0;
        public int defaultPageSize = 5;
        public double largeTransferLimit = 100000.0;
        public double minimumAmount = 0.0;
        public int recentTransactionsLimit = 5;
    }

    public static class Interest {
        public double savingsAnnualRate = 4.0;
        public double monthsPerYear = 12.0;
        public double percentageDivisor = 100.0;
    }

    public static class FixedDeposit {
        public int longTermThresholdMonths = 12;
        public double longTermAnnualRate = 7.5;
        public double shortTermAnnualRate = 5.5;
        public int compoundingPerYear = 4;
        public double percentageDivisor = 100.0;
        public double monthsPerYear = 12.0;
    }

    public static class Loan {
        public double monthsPerYear = 12.0;
        public double percentageDivisor = 100.0;
        public double emiRoundingScale = 100.0;
    }

    public static class Dashboard {
        public int relationshipAssignedClients = 12;
        public double relationshipTotalAum = 4500000.0;
        public int superAdminActiveSessions = 142;
        public double tellerCashInDrawer = 450000.0;
        public double managerBranchDeposits = 15000000.0;
        public int complianceFlaggedTransactions = 15;
        public int complianceAuditLogCount = 340;
    }

    public static class ManagerReports {
        public double totalDeposits = 1500000.0;
        public double totalWithdrawals = 450000.0;
        public int newAccountsOffset = 5;
    }

    public static class DebitCard {
        public int segmentBase = 1000;
        public int segmentRandomBound = 9000;
        public int cvvBase = 100;
        public int cvvRandomBound = 900;
        public int expiryYears = 5;
    }

    public static class SystemStats {
        public int bytesPerKilobyte = 1024;
        public int kilobytesPerMegabyte = 1024;
        public int millisecondsPerMinute = 60000;
    }

    public static class Pdf {
        public int titleFontSize = 18;
        public int subtitleFontSize = 14;
        public int normalFontSize = 12;
        public int headerFontSize = 12;
        public int footnoteFontSize = 10;
        public int twoColumnTableColumns = 2;
        public int threeColumnTableColumns = 3;
        public int tableWidthPercentage = 100;
        public int statementColumnWidth = 3;
        public int headerBorderWidth = 1;
        public int cellPadding = 5;
    }
}
