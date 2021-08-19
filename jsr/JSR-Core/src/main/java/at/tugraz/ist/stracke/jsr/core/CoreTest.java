package at.tugraz.ist.stracke.jsr.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoreTest {
    private static final Logger logger = LogManager.getLogger(CoreTest.class);
    public void hello() {
        logger.traceEntry();
        logger.info("Hello From Core logger Info");
        logger.warn("Hello From Core logger Warn");
        logger.error("Hello From Core logger Error");
        logger.fatal("Hello From Core logger Fatal");
        logger.debug("Hello From Core Logger Debug");
        System.out.println("Hello From core");
    }
}
