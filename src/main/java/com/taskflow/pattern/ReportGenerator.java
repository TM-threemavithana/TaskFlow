package com.taskflow.pattern;

import java.util.Map;

/**
 * DESIGN PATTERN: TEMPLATE METHOD (behavioral) - standalone demo.
 *
 * <p>PROBLEM: Generating different report formats (text, HTML, CSV) shares a common ALGORITHM:
 * 1. Generate header → 2. Generate body → 3. Generate footer.
 * The overall structure is the same, but the details of each step differ.
 *
 * <p>TEMPLATE METHOD SOLUTION: Define the skeleton of the algorithm in a BASE CLASS method
 * ({@code generateReport()} - the "template method"), and let subclasses override specific steps.
 *
 * <p>Key rule: the template method itself is {@code final} so subclasses cannot change the
 * algorithm's structure - they can only fill in the varying parts.
 *
 * <p>USED IN THE REAL APP: {@code OncePerRequestFilter} (extended by JwtAuthenticationFilter)
 * is a template method: the base class handles "should this filter run?" logic and calls
 * the abstract {@code doFilterInternal()} that we implement.
 *
 * <h2>ABSTRACT CLASS vs INTERFACE</h2>
 * <ul>
 *   <li>INTERFACE = a CONTRACT ("what you can do"):  pure abstract methods, no state.
 *       A class can implement MULTIPLE interfaces.</li>
 *   <li>ABSTRACT CLASS = a PARTIAL IMPLEMENTATION ("here's some shared behavior, fill in the rest"):
 *       can have fields, constructors, concrete methods AND abstract methods.
 *       A class can extend only ONE abstract class (single inheritance).</li>
 *   <li>USE INTERFACE when different unrelated classes share a capability (e.g. Comparable, Serializable).</li>
 *   <li>USE ABSTRACT CLASS when classes share common code/state and form an IS-A hierarchy
 *       (e.g. BaseEntity → User, TextReportGenerator → TextReport).</li>
 * </ul>
 */
public abstract class ReportGenerator {

    /** The TEMPLATE METHOD: defines the algorithm skeleton. It is final - subclasses cannot change the order. */
    public final String generateReport(String title, Map<String, Object> data) {
        StringBuilder report = new StringBuilder();
        report.append(generateHeader(title));
        report.append(generateBody(data));
        report.append(generateFooter());
        return report.toString();
    }

    // ---- Abstract steps: subclasses MUST implement these ----
    protected abstract String generateHeader(String title);
    protected abstract String generateBody(Map<String, Object> data);
    protected abstract String generateFooter();

    // ---- Concrete subclasses ----

    /** Plain text report. */
    public static class TextReportGenerator extends ReportGenerator {
        @Override
        protected String generateHeader(String title) {
            return "=== " + title + " ===\n";
        }

        @Override
        protected String generateBody(Map<String, Object> data) {
            StringBuilder sb = new StringBuilder();
            data.forEach((key, value) -> sb.append(key).append(": ").append(value).append("\n"));
            return sb.toString();
        }

        @Override
        protected String generateFooter() {
            return "=== End of Report ===\n";
        }
    }

    /** HTML report. */
    public static class HtmlReportGenerator extends ReportGenerator {
        @Override
        protected String generateHeader(String title) {
            return "<html><body><h1>" + title + "</h1>\n";
        }

        @Override
        protected String generateBody(Map<String, Object> data) {
            StringBuilder sb = new StringBuilder("<ul>\n");
            data.forEach((key, value) -> sb.append("  <li><b>").append(key).append("</b>: ").append(value).append("</li>\n"));
            sb.append("</ul>\n");
            return sb.toString();
        }

        @Override
        protected String generateFooter() {
            return "</body></html>\n";
        }
    }
}
