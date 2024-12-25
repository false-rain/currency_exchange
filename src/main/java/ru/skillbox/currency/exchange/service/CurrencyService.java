package ru.skillbox.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.skillbox.currency.exchange.dto.CurrencyDto;
import ru.skillbox.currency.exchange.dto.CurrencyShortDto;
import ru.skillbox.currency.exchange.entity.Currency;
import ru.skillbox.currency.exchange.mapper.CurrencyMapper;
import ru.skillbox.currency.exchange.repository.CurrencyRepository;
import ru.skillbox.currency.exchange.xml.CurrencyXml;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyMapper mapper;
    private final CurrencyRepository repository;

    public CurrencyDto getById(Long id) {
        log.info("CurrencyService method getById executed");
        Currency currency = repository.findById(id).orElseThrow(() -> new RuntimeException("Currency not found with id: " + id));
        return mapper.convertToDto(currency);
    }

    public Double convertValue(Long value, Long numCode) {
        log.info("CurrencyService method convertValue executed");
        Currency currency = repository.findByIsoNumCode(numCode);
        return value * currency.getValue();
    }

    public CurrencyDto create(CurrencyDto dto) {
        log.info("CurrencyService method create executed");
        return  mapper.convertToDto(repository.save(mapper.convertToEntity(dto)));
    }

    public List<CurrencyShortDto> getAllCurrenciesShort() {
        return repository.findAll().stream()
                .map(currency -> new CurrencyShortDto(currency.getName(), currency.getValue()))
                .collect(Collectors.toList());
    }

    public List<Currency> createCurrenciesFromXml(List<CurrencyXml> currencyXmlList) {
        List<Currency> list = new ArrayList<>();
        currencyXmlList.forEach(e->{
            list.add(convertXmlToCurrency(e));
        });
        return list;
    }

    public List<Currency> createOrUpdateCurrenciesFromXml(List<CurrencyXml> currencyXmlList) {
        List<Currency> list = new ArrayList<>();
        currencyXmlList.forEach(e->{
            list.add(createOrUpdateCurrency(e));
        });
        return list;
    }

    private Currency convertXmlToCurrency(CurrencyXml currencyXml){
        Currency currency = new Currency();
        fillCurrency(currency, currencyXml);
        return repository.save(currency);
    }

    private Currency createOrUpdateCurrency(CurrencyXml currencyXml) {
        Currency currency = repository.findByXmlId(currencyXml.getId())
                .orElse(new Currency());
        fillCurrency(currency, currencyXml);
        return repository.save(currency);
    }

    private void fillCurrency(Currency currency, CurrencyXml currencyXml) {
        currency.setIsoCharCode(currencyXml.getCharCode());
        currency.setIsoNumCode((long) currencyXml.getNumCode());
        currency.setName(currencyXml.getName());
        currency.setNominal((long) currencyXml.getNominal());
        try {
            NumberFormat format = DecimalFormat.getInstance(Locale.FRANCE);
            Number number = format.parse(currencyXml.getValue());
            currency.setValue(number.doubleValue());
        } catch (ParseException e) {
            currency.setValue(0.0);
            log.warn(e.getMessage());
        }
        currency.setXmlId(currencyXml.getId());
    }
}
