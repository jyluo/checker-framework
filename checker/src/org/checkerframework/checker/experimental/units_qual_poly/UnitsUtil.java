package org.checkerframework.checker.experimental.units_qual_poly;

import org.checkerframework.checker.units.qual.Units;
import org.checkerframework.framework.qual.EnsuresQualifierIf;
import org.checkerframework.framework.qual.EnsuresQualifiersIf;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.Units.qual.*;
import org.checkerframework.dataflow.qual.*;
*/

// This class should be kept in sync with plume.UnitsUtil .

/**
 * Utility methods for regular expressions, most notably for testing whether
 * a string is a regular expression.
 * <p>
 *
 * For an example of intended use, see section <a
 * href="http://types.cs.washington.edu/checker-framework/current/checker-framework-manual.html#Unitsutil-methods">Testing
 * whether a string is a regular expression</a> in the Checker Framework
 * manual.
 * <p>
 *
 * <b>Runtime Dependency</b>:
 * Using this class introduces a runtime dependency.
 * This means that you need to distribute (or link to) the Checker
 * Framework, along with your binaries.
 * To eliminate this dependency, you can simply copy this class into your
 * own project.
 */
// The PurityChecker cannot show for most methods in this class that
// they are pure, even though they are.
@SuppressWarnings("purity")
public class UnitsUtil {

  /**
   * A checked version of {@link PatternSyntaxException}.
   * <p>
   * This exception is useful when an illegal Units is detected but the
   * contextual information to report a helpful error message is not available
   * at the current depth in the call stack. By using a checked
   * PatternSyntaxException the error must be handled up the call stack where
   * a better error message can be reported.
   * <p>
   *
   * Typical usage is:
   * <pre>
   * void myMethod(...) throws CheckedPatternSyntaxException {
   *   ...
   *   if (! isUnits(myString)) {
   *     throw new CheckedPatternSyntaxException(...);
   *   }
   *   ... Pattern.compile(myString) ...
   * </pre>
   *
   * Simply calling <tt>Pattern.compile</tt> would have a similar effect,
   * in that <tt>PatternSyntaxException</tt> would be thrown at run time if
   * <tt>myString</tt> is not a regular expression.  There are two problems
   * with such an approach.  First, a client of <tt>myMethod</tt> might
   * forget to handle the exception, since <tt>PatternSyntaxException</tt>
   * is not checked.  Also, the Units Checker would issue a warning about
   * the call to <tt>Pattern.compile</tt> that might throw an exception.
   * The above usage pattern avoids both problems.
   *
   * @see PatternSyntaxException
   */
  public static class CheckedPatternSyntaxException extends Exception {

    private static final long serialVersionUID = 6266881831979001480L;

    private final PatternSyntaxException pse;

    /**
     * Constructs a new CheckedPatternSyntaxException equivalent to the
     * given {@link PatternSyntaxException}.
     * <p>
     * Consider calling this constructor with the result of
     * {@link UnitsUtil#UnitsError}.
     * @param pse the PatternSyntaxException to be wrapped
     */
    public CheckedPatternSyntaxException(PatternSyntaxException pse) {
      this.pse = pse;
    }

    /**
     * Constructs a new CheckedPatternSyntaxException.
     *
     * @param desc A description of the error
     * @param Units The erroneous pattern
     * @param index The approximate index in the pattern of the error,
     *              or {@code -1} if the index is not known
     */
    public CheckedPatternSyntaxException(String desc, String Units, int index) {
      this(new PatternSyntaxException(desc, Units, index));
    }

    /**
     * Retrieves the description of the error.
     *
     * @return The description of the error
     */
    public String getDescription() {
      return pse.getDescription();
    }

    /**
     * Retrieves the error index.
     *
     * @return The approximate index in the pattern of the error, or {@code -1}
     *         if the index is not known
     */
    public int getIndex() {
      return pse.getIndex();
    }

    /**
     * Returns a multi-line string containing the description of the syntax
     * error and its index, the erroneous regular-expression pattern, and a
     * visual indication of the error index within the pattern.
     *
     * @return The full detail message
     */
    @Override
    public String getMessage() {
      return pse.getMessage();
    }

    /**
     * Retrieves the erroneous regular-expression pattern.
     *
     * @return The erroneous pattern
     */
    public String getPattern() {
      return pse.getPattern();
    }
  }

  private UnitsUtil() {
    throw new AssertionError("Class UnitsUtil shouldn't be instantiated");
  }

  /**
   * Returns true if the argument is a syntactically valid regular
   * expression.
   * @param s string to check for being a regular expression
   * @return true iff s is a regular expression
   */
  /*@Pure*/
  @EnsuresQualifiersIf({
          @EnsuresQualifierIf(result=true, expression="#1", qualifier=Units.class)})
  public static boolean isUnits(String s) {
    return isUnits(s, 0);
  }

  /**
   * Returns true if the argument is a syntactically valid regular
   * expression with at least the given number of groups.
   * @param s string to check for being a regular expression
   * @param groups number of groups expected
   * @return true iff s is a regular expression with groups groups
   */
  /*>>>
  @SuppressWarnings("Units")    // UnitsUtil
  */
  /*@Pure*/
  // No @EnsuresQualifierIf annotation because this method is special-cased
  // in UnitsTransfer.
  public static boolean isUnits(String s, int groups) {
    Pattern p;
    try {
      p = Pattern.compile(s);
    } catch (PatternSyntaxException e) {
      return false;
    }
    return getGroupCount(p) >= groups;
  }

  /**
   * Returns true if the argument is a syntactically valid regular
   * expression.
   * @param c char to check for being a regular expression
   * @return true iff c is a regular expression
   */
  /*>>>
  @SuppressWarnings("Units")    // UnitsUtil
  */
  /*@Pure*/
  @EnsuresQualifiersIf({
          @EnsuresQualifierIf(result=true, expression="#1", qualifier=Units.class)})
  public static boolean isUnits(final char c) {
    return isUnits(Character.toString(c));
  }

  /**
   * Returns null if the argument is a syntactically valid regular
   * expression. Otherwise returns a string describing why the argument is
   * not a Units.
   * @param s string to check for being a regular expression
   * @return null, or a string describing why the argument is not a Units.
   */
  /*>>>
  @SuppressWarnings("Units")    // UnitsUtil
  */
  /*@SideEffectFree*/
  public static /*@Nullable*/ String UnitsError(String s) {
    return UnitsError(s, 0);
  }

  /**
   * Returns null if the argument is a syntactically valid regular
   * expression with at least the given number of groups. Otherwise returns
   * a string describing why the argument is not a Units.
   * @param s string to check for being a regular expression
   * @param groups number of groups expected
   * @return null, or a string describing why the argument is not a Units.
   */
  /*>>>
  @SuppressWarnings("Units")    // UnitsUtil
  */
  /*@SideEffectFree*/
  public static /*@Nullable*/ String UnitsError(String s, int groups) {
    try {
      Pattern p = Pattern.compile(s);
      int actualGroups = getGroupCount(p);
      if (actualGroups < groups) {
        return UnitsErrorMessage(s, groups, actualGroups);
      }
    } catch (PatternSyntaxException e) {
      return e.getMessage();
    }
    return null;
  }

  /**
   * Returns null if the argument is a syntactically valid regular
   * expression. Otherwise returns a PatternSyntaxException describing
   * why the argument is not a Units.
   * @param s string to check for being a regular expression
   * @return null, or a PatternSyntaxException describing why the argument is not a Units.
   */
  /*>>>
  @SuppressWarnings("Units")    // UnitsUtil
  */
  /*@SideEffectFree*/
  public static /*@Nullable*/ PatternSyntaxException UnitsException(String s) {
    return UnitsException(s, 0);
  }

  /**
   * Returns null if the argument is a syntactically valid regular
   * expression with at least the given number of groups. Otherwise returns a
   * PatternSyntaxException describing why the argument is not a Units.
   * @param s string to check for being a regular expression
   * @param groups number of groups expected
   * @return null, or a PatternSyntaxException describing why the argument is not a Units.
   */
  /*>>>
  @SuppressWarnings("Units")    // UnitsUtil
  */
  /*@SideEffectFree*/
  public static /*@Nullable*/ PatternSyntaxException UnitsException(String s, int groups) {
    try {
      Pattern p = Pattern.compile(s);
      int actualGroups = getGroupCount(p);
      if (actualGroups < groups) {
        return new PatternSyntaxException(UnitsErrorMessage(s, groups, actualGroups), s, -1);
      }
    } catch (PatternSyntaxException pse) {
      return pse;
    }
    return null;
  }

  /**
   * Returns the argument as a {@code @Units String} if it is a Units,
   * otherwise throws an error. The purpose of this method is to suppress Units
   * Checker warnings. Once the Units Checker supports flow-sensitivity, it
   * should be very rarely needed.
   * @param s string to check for being a regular expression
   * @return its argument
   * @throws Error if argument is not a Units
   */
  /*@SideEffectFree*/
  // The return type annotation is a conservative bound.
  public static /*@Units*/
  String asUnits(String s) {
    return asUnits(s, 0);
  }

  /**
   * Returns the argument as a {@code @Units(groups) String} if it is a Units
   * with at least the given number of groups, otherwise throws an error. The
   * purpose of this method is to suppress Units Checker warnings. Once the
   * Units Checker supports flow-sensitivity, it should be very rarely needed.
   * @param s string to check for being a regular expression
   * @param groups number of groups expected
   * @return its argument
   * @throws Error if argument is not a Units
   */
  /*>>>
  @SuppressWarnings("Units")    // UnitsUtil
  */
  /*@SideEffectFree*/
  // The return type annotation is irrelevant; it is special-cased by
  // UnitsAnnotatedTypeFactory.
  public static /*@Units*/
  String asUnits(String s, int groups) {
    try {
      Pattern p = Pattern.compile(s);
      int actualGroups = getGroupCount(p);
      if (actualGroups < groups) {
        throw new Error(UnitsErrorMessage(s, groups, actualGroups));
      }
      return s;
    } catch (PatternSyntaxException e) {
      throw new Error(e);
    }
  }

  /**
   * Generates an error message for s when expectedGroups are needed, but s
   * only has actualGroups.
   * @param s string to check for being a regular expression
   * @return an error message for s when expectedGroups groups are needed, but s
   * only has actualGroups groups
   */
  private static String UnitsErrorMessage(String s, int expectedGroups, int actualGroups) {
    return "Units \"" + s + "\" has " + actualGroups + " groups, but " +
        expectedGroups + " groups are needed.";
  }

  /**
   * Return the count of groups in the argument.
   * @param p pattern whose groups to count
   * @return the count of groups in the argument
   */
  /*@Pure*/
  private static int getGroupCount(Pattern p) {
    return p.matcher("").groupCount();
  }
}
