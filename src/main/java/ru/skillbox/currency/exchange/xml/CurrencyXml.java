package ru.skillbox.currency.exchange.xml;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlRootElement(name = "Valute")
@XmlAccessorType(XmlAccessType.FIELD)
public class CurrencyXml {

    @XmlAttribute(name = "ID")
    private String id;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Nominal")
    private int nominal;

    @XmlElement(name = "Value")
    private String value;

    @XmlElement(name = "NumCode")
    private int numCode;

    @XmlElement(name = "CharCode")
    private String charCode;

}