package org.liquidsql.config;

import org.liquidsql.util.LiquidException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class ConfigXmlReader
{
    private static final String XML_CONFIG_FILE_NAME = "liquid-sql-config.xml";

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Document document;

    /**
     * Protected constructor
     */
    protected ConfigXmlReader() {}

    /**
     * Reads the configuration settings from the XML file
     * in the classpath
     * @return LiquidConfiguration
     *
     * @throws LiquidException
     */
    protected LiquidConfiguration readConfigurationFromXmlFile() throws LiquidException
    {
        try
        {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream =classLoader.getResourceAsStream(XML_CONFIG_FILE_NAME);

            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(inputStream);

            document.getDocumentElement().normalize();

            NodeList configurationNodeList = document.getElementsByTagName("configuration");
            LiquidConfiguration configuration = new LiquidConfiguration();

            for (int i = 0; i < configurationNodeList.getLength(); i++)
            {
                Node node = configurationNodeList.item(i);
                NodeList propertyNodeList = node.getChildNodes();
                
                for (int j = 0; j < propertyNodeList.getLength(); j++)
                {
                    Node propertyNode = propertyNodeList.item(j);
                    
                    if (propertyNode.getNodeType() == Node.ELEMENT_NODE && propertyNode.getNodeName().equals("property"))
                    {
                        Element element = (Element) propertyNode;

                        String propertyName = element.getAttribute("name");
                        String propertyValue = element.getTextContent();

                        configuration.addConfigurationProperty(propertyName, propertyValue);
                    }
                }
            }

            return configuration;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }
}
