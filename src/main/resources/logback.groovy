import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import java.nio.charset.StandardCharsets

/**
 * Configuration variables from possible inputs
 * - system environment - to provide configuration for specific environment
 * - application.properties - to provide default configuration
 * - application-${spring.profiles.active}.properties - useful for local development
 */
def applicationProps = new Properties()
PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader())
resolver.getResources("classpath*:application.properties").each { applicationProps.load(it.inputStream) }
def springActiveProfile = System.getProperty("spring.profiles.active")
if (springActiveProfile) {
    resolver.getResources("classpath*:application-${springActiveProfile}.properties")
            .each { applicationProps.load(it.inputStream) }
}

def env = applicationProps + System.getenv()
def SERVICE_NAME = env.get("spring.application.name", "-")

def APPENDERS = env.get("app.logback.appenders", "stdout").split(",") as List

// Loggin levels and packages
def ROOT_LOGGER_LEVEL = valueOf(env["logging.level.root"] ?: "INFO")

println "================================================"
println " You are using logback.groovy"
println " Service = $SERVICE_NAME"

println " app.logback.appenders = $APPENDERS"
println " logging.level.root = $ROOT_LOGGER_LEVEL"
println "================================================"

/**
 * Appenders
 */
if (APPENDERS.contains("stdout")) {
    appender("stdout", ConsoleAppender) {
        target = "System.out"
        encoder(PatternLayoutEncoder) {
            charset = StandardCharsets.UTF_8
            pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p %t %c{0}:%M:%L - %m%n"
        }
    }
}

root(ROOT_LOGGER_LEVEL, APPENDERS)
