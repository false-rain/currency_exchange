package ru.skillbox.currency.exchange.scheduler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.skillbox.currency.exchange.service.CurrencyService;
import ru.skillbox.currency.exchange.xml.CurrenciesXml;
import ru.skillbox.currency.exchange.xml.CurrencyXml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.List;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "data")
public class CurrencyScheduler {

    private String url;

    @Value("${data.interval}")
    private String interval;

    @Autowired
    private CurrencyService currencyService;

    @Scheduled(fixedRateString = "#{T(java.time.Duration).parse('${data.interval}').toMillis()}")
    public void updateCurrencies() {
        try {
            String xmlData = xmlData();
            List<CurrencyXml> currencyXmlList = getListOfNewData(xmlData);
            currencyService.createOrUpdateCurrenciesFromXml(currencyXmlList);
            log.info("Data is updated");
        } catch (Exception e) {
            log.warn(e.toString());
        }
    }

    private String xmlData() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject (url, String.class);
    }

    private List<CurrencyXml> getListOfNewData(String xmlData) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CurrenciesXml.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xmlData);
        CurrenciesXml currenciesXml = (CurrenciesXml) unmarshaller.unmarshal(reader);
        return currenciesXml.getCurrencies();
    }
}
