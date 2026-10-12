package com.chain.platform.core.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AnalyzeClasses(
    packages = "com.chain.platform.core",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final String ROOT = "com.chain.platform.core.";

  private static final List<String> MODULES =
      List.of("identityaccess", "store", "catalog", "pricing", "inventory", "orders", "reporting");

  /** Module được phép gọi (qua package api). identityaccess và shared luôn được phép. */
  private static final Map<String, Set<String>> ALLOWED =
      Map.of(
          "identityaccess", Set.of(),
          "store", Set.of(),
          "catalog", Set.of(),
          "pricing", Set.of("catalog"),
          "inventory", Set.of("store", "catalog"),
          "orders", Set.of("store", "catalog", "pricing", "inventory"),
          "reporting", Set.of("orders", "inventory"));

  @ArchTest
  static void only_api_is_visible_outside_module(JavaClasses classes) {
    for (String m : MODULES) {
      String p = ROOT + m;
      noClasses()
          .that()
          .resideOutsideOfPackage(p + "..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              p + ".controller..", p + ".service..", p + ".domain..", p + ".repository..")
          .allowEmptyShould(true)
          .because("module " + m + " chỉ lộ package api")
          .check(classes);
    }
  }

  @ArchTest
  static void modules_follow_dependency_matrix(JavaClasses classes) {
    for (String m : MODULES) {
      for (String other : MODULES) {
        if (m.equals(other) || other.equals("identityaccess") || ALLOWED.get(m).contains(other)) {
          continue;
        }
        noClasses()
            .that()
            .resideInAPackage(ROOT + m + "..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(ROOT + other + "..")
            .allowEmptyShould(true)
            .because(m + " không được phụ thuộc " + other)
            .check(classes);
      }
    }
  }

  @ArchTest
  static void shared_depends_on_no_module(JavaClasses classes) {
    for (String m : MODULES) {
      noClasses()
          .that()
          .resideInAPackage(ROOT + "shared..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage(ROOT + m + "..")
          .allowEmptyShould(true)
          .check(classes);
    }
  }

  @ArchTest
  static final ArchRule no_cycles_between_modules =
      slices().matching(ROOT + "(*)..").should().beFreeOfCycles().allowEmptyShould(true);

  @ArchTest
  static void layering_inside_module(JavaClasses classes) {
    for (String m : MODULES) {
      String p = ROOT + m;
      noClasses()
          .that()
          .resideInAPackage(p + ".controller..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage(p + ".repository..")
          .allowEmptyShould(true)
          .because("controller đi qua service")
          .check(classes);
      noClasses()
          .that()
          .resideInAPackage(p + ".domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework.web..")
          .allowEmptyShould(true)
          .check(classes);
      noClasses()
          .that()
          .resideInAPackage(p + ".repository..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(p + ".controller..", p + ".service..")
          .allowEmptyShould(true)
          .check(classes);
    }
  }
}
