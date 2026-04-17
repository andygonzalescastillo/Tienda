package com.tienda.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

/**
 * Hints de reflection y recursos para GraalVM Native Image.
 * Spring AOT maneja la mayoría automáticamente, pero algunas
 * librerías de terceros necesitan configuración manual.
 */
@Configuration
@ImportRuntimeHints(NativeHints.AppRuntimeHints.class)
public class NativeHints {

    @Slf4j
    static class AppRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            registerJjwtHints(hints);
            registerAppHints(hints);
            registerResourceHints(hints);
        }

        /**
         * Registra DTOs (records), entidades y clases internas
         * de la app que GraalVM necesita conocer por reflection.
         */
        private void registerAppHints(RuntimeHints hints) {
            try {
                PathMatchingResourcePatternResolver resolver =
                        new PathMatchingResourcePatternResolver();
                // Escanear todos los DTOs, entidades y config del proyecto
                String[] patterns = {
                        "classpath*:com/tienda/backend/dto/**/*.class",
                        "classpath*:com/tienda/backend/domain/**/*.class",
                        "classpath*:com/tienda/backend/config/properties/**/*.class"
                };

                CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

                for (String pattern : patterns) {
                    Resource[] resources = resolver.getResources(pattern);
                    for (Resource resource : resources) {
                        if (resource.isReadable()) {
                            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                            String className = metadataReader.getClassMetadata().getClassName();

                            hints.reflection().registerType(
                                    TypeReference.of(className),
                                    MemberCategory.values()
                            );
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error registrando hints de reflection para DTOs/entidades", e);
            }
        }

        /**
         * JJWT usa reflection para instanciar implementaciones
         * de algoritmos de firma y parsers.
         */
        private void registerJjwtHints(RuntimeHints hints) {
            try {
                // Escanea dinámicamente TODAS las clases de JJWT en el classpath
                // y las registra para reflection en GraalVM. ¡A prueba de balas!
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
               Resource[] resources = resolver.getResources("classpath*:io/jsonwebtoken/**/*.class");
                
                CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
                
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                        String className = metadataReader.getClassMetadata().getClassName();
                        
                        hints.reflection().registerType(
                                TypeReference.of(className),
                                MemberCategory.values()
                        );
                    }
                }
            } catch (Exception e) {
                log.warn("Error registrando hints de JJWT para reflection", e);
            }
        }

        /**
         * Recursos que deben estar disponibles en runtime:
         * - Templates de Thymeleaf (emails)
         * - Migraciones de Flyway
         */
        private void registerResourceHints(RuntimeHints hints) {
            // Templates de email (Thymeleaf)
            hints.resources().registerPattern("templates/*");

            // Migraciones de Flyway
            hints.resources().registerPattern("db/migration/*");

            // Application config
            hints.resources().registerPattern("application*.yml");
        }
    }
}
