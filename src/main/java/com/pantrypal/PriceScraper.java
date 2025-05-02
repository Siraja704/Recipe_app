package com.pantrypal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class PriceScraper {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "F:\\Areeb\\SOFTWARES\\Other Softwares\\Intellige\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.walmart.com/");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
        searchBox.sendKeys("chicken breast");
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'search-icon')]")));
        searchButton.click();
        WebDriverWait priceWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        priceWait.until(ExpectedConditions.visibilityOfElementLocated(By.className("price-characteristic")));
        List<WebElement> prices = driver.findElements(By.className("price-characteristic"));
        if (!prices.isEmpty()) {
            String price = prices.get(0).getText();
            System.out.println("Price of Chicken Breast: $" + price);
        } else {
            System.out.println("No price found for the ingredient.");
        }
        driver.quit();
    }
}