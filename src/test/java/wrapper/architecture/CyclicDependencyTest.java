package wrapper.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;


class CyclicDependencyTest {

    @Test
    void packagesMustBeFreeOfCycles() {
        final JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(List.of("..wrapper.."));

        assertFalse(importedClasses.isEmpty());
        SlicesRuleDefinition.slices()
                .matching("..(*)..")
                .should().beFreeOfCycles()
                .check(importedClasses);
    }

}
