package runner;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.fetch.Fetch;
import org.openqa.selenium.devtools.v85.fetch.model.RequestPattern;
import org.testng.Assert;
import org.testng.annotations.Test;

import baseClass.BaseClass;

public class ThirdClassNetworkIntercept extends BaseClass {

	@Test
	public void launchingAndMockingResponse() throws MalformedURLException {
		ChromeOptions options=new ChromeOptions();
		options.addArguments("--start-maximized");
		//ChromeDriver driver=new ChromeDriver(options);
		ChromeDriver driver2 = (ChromeDriver)getDriver();
		driver2.manage().window().maximize();
		driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
		driver2.get("https://rahulshettyacademy.com/client");
		driver2.findElement(By.cssSelector("#userEmail")).sendKeys("ayyappangunasekaran5@gmail.com");
		driver2.findElement(By.cssSelector("#userPassword")).sendKeys("9442768022");
		driver2.findElement(By.cssSelector("#login")).click();
		//674007b5ae2afd4c0bc8d134
		
		driver2.findElement(By.xpath("//button[contains(@routerlink,'/dashboard/myorders')]")).click();
		DevTools devTools = driver2.getDevTools();
		devTools.createSession();
		devTools.send(Fetch.enable(
				Optional.of(
						List.of(
								new RequestPattern(Optional.of("https://rahulshettyacademy.com/api/ecom/order/get-orders-details?id=*"), 
						Optional.empty(), 
						Optional.empty()
						)	
						)), 
				Optional.empty()));
		
		devTools.addListener(Fetch.requestPaused(), request->{
			if(request.getRequest().getUrl().contains("https://rahulshettyacademy.com/api/ecom/order/get-orders-details?id")) {
				String mockREquest="https://rahulshettyacademy.com/api/ecom/order/get-orders-details?id=674007b5ae2afd4c0bc8d134";
				devTools.send(Fetch.continueRequest
						(request.getRequestId(), 
								Optional.empty(), 
								Optional.of(mockREquest), 
								Optional.empty(), 
								Optional.empty()));
			}else {
				devTools.send(Fetch.continueRequest(request.getRequestId(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
			}
		});
		driver2.findElements(By.xpath("//button[text()='View']")).get(0).click();
		WebElement findElement = driver2.findElement(By.xpath("//p[text()='You are not authorize to view this order']"));
		Assert.assertTrue(findElement.getText().contains(" not authorize"),"Order is visible for this user");
	}
}
