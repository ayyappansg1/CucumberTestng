package runner;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import constants.Constants;
import cucumberReporting.Reporting;
import driverManager.DriverManager;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import utils.CommonUtils;

/**
 * This is a Runner Class Execution starts from here.Additional runner classes
 * also can be Maintained
 */

@CucumberOptions(dryRun = false, features = "src\\test\\resources\\FeatureFiles", glue = {
		"stepdefinitions" }, monochrome = true, plugin = { "rerun:target/failed.txt", "json:target/forReporting.json",
				"html:target/sangar.html", "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
				"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" }
//tags = "not @smoke"
)
public class RunnerParallelScenario extends AbstractTestNGCucumberTests {
	private static final Logger logger = LogManager.getLogger(RunnerParallelScenario.class);
	protected MultiPartEmail email;

	@BeforeSuite
	public void aa() {
		CommonUtils.cleanAllureResults();
	}

	@Parameters("browser")
	@BeforeMethod(alwaysRun = true)
	public void parallelCheck(String browser) throws IOException {
		if (browser == null) {
			throw new IllegalArgumentException("Browser parameter not provided in TestNG suite.");
		}
		DriverManager.setBrowserName(browser);
		logger.info("Before method running for browser: " + DriverManager.getBrowserName());
		logger.info(Thread.currentThread().getId() + " Before setting browser: " + browser);
		CommonUtils.loadProperties(DriverManager.getBrowserName());
	}

	@Override
	@DataProvider(parallel = true)
	public Object[][] scenarios() {
		logger.info("DataProvider is supplying scenarios for thread: " + Thread.currentThread().getId());
		return super.scenarios();
	}
//	@AfterClass(alwaysRun = true)
//	public void globalTearDown() {
//		logger.info("Inside After Class for Driver Quit");
//		DriverManager.quitDriver();
//		}

//	@AfterSuite(alwaysRun = true)
//	public void generateReporting() {
//		Reporting.generateReport(System.getProperty("user.dir")+File.separator+"target"+File.separator+"forReporting.json");
//	}
//	@Parameters("browser")
//	@BeforeSuite(alwaysRun = true)
//	public void setupSuite(String browser) {
//	    if (browser == null) {
//	        throw new IllegalArgumentException("Browser parameter not provided in TestNG suite.");
//	    }
//	    DriverManager.setBrowserName(browser);
//	    logger.info("Browser set in @BeforeSuite: " + browser);
//	}
	@AfterSuite(alwaysRun = true)
	public void sendEmail() throws EmailException {
		// generateAllureReport(); // Step 1: Generate Allure report
		// File allureZip = zipAllureReport(); // Step 2: Zip the report

		StringBuilder emailContent = new StringBuilder();
		emailContent.append("Test Results: ");

		// Attach Extent report
		EmailAttachment extentAttachment = new EmailAttachment();
		extentAttachment.setPath(System.getProperty("user.dir") + File.separator + "target" + File.separator
				+ "ExtentReport" + File.separator + "HeroKuAppAutomation.html");
		extentAttachment.setDisposition(EmailAttachment.ATTACHMENT);
		extentAttachment.setDescription("Selenium Extent Report");
		extentAttachment.setName("ExtentReport.html");

		// Attach Allure report zip
//		    EmailAttachment allureAttachment = new EmailAttachment();
//		    allureAttachment.setPath(allureZip.getAbsolutePath());
//		    allureAttachment.setDisposition(EmailAttachment.ATTACHMENT);
//		    allureAttachment.setDescription("Allure Report");
//		    allureAttachment.setName("AllureReport.zip");

		email = new MultiPartEmail();
		email.setHostName("smtp.zoho.in");
		email.setSmtpPort(465);
		email.setSSLOnConnect(true);
		email.setAuthentication("ayyappansg1@zohomail.in", "FArvYhxi2Emk");
		email.setFrom("ayyappansg1@zohomail.in");
		email.addTo("ayyappangunasekaran5@gmail.com");
		email.setMsg("Hi sir, Automated Mail");
		email.setSubject("HerokuApp automation result-" + emailContent.toString() + "- " + dateAndTime());
		email.setSocketConnectionTimeout(60000);
		email.setSocketTimeout(60000);

		email.attach(extentAttachment);
		// email.attach(allureAttachment); // Attach the zipped Allure report

		try {
			email.send();
			logger.info("Email sent successfully");
		} catch (EmailException e) {
			logger.error("Error sending email: " + e.getMessage());
		}
	}

//		private void generateAllureReport() {
//		    try {
//		        Runtime.getRuntime().exec("allure generate --clean");
//		    } catch (IOException e) {
//		        logger.error("Failed to generate Allure report: " + e.getMessage());
//		    }
//		}
	//
//		private File zipAllureReport() {
//		    Path sourceDir = Paths.get(System.getProperty("user.dir"), "allure-report");
//		    File zipFile = new File(System.getProperty("user.dir") + File.separator + "allure-report.zip");
//		    
//		    try (FileOutputStream fos = new FileOutputStream(zipFile);
//		         ZipOutputStream zos = new ZipOutputStream(fos)) {
//		        
//		        Files.walk(sourceDir)
//		            .filter(path -> !Files.isDirectory(path))
//		            .forEach(path -> {
//		                ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
//		                try (FileInputStream fis = new FileInputStream(path.toFile())) {
//		                    zos.putNextEntry(zipEntry);
//		                    byte[] buffer = new byte[1024];
//		                    int length;
//		                    while ((length = fis.read(buffer)) > 0) {
//		                        zos.write(buffer, 0, length);
//		                    }
//		                    zos.closeEntry();
//		                } catch (IOException e) {
//		                    logger.error("Error zipping file: " + e.getMessage());
//		                }
//		            });
//		    } catch (IOException e) {
//		        logger.error("Failed to create zip file: " + e.getMessage());
//		    }
//		    return zipFile;
//		}

	public String dateAndTime() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMMyyyy h.mma");
		String formattedDateTime = currentDateTime.format(formatter);
		return formattedDateTime;

	}
}
