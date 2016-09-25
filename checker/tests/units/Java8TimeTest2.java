import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ValueRange;
import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;

@TimeInstant
class Alpha {
    @TimeInstant
    public Alpha() {}

    void m() {
        // default receiver is @Scalar
    }
}

@TimeInstant
class Beta {
    @TimeInstant
    public Beta() {}

    void m(@TimeInstant Beta this) {
        // receiver is a @TimeInstant
    }
}

class Other {
    void Gamma() {
        Alpha a = new @CALyear Alpha();
        //:: error: (method.invocation.invalid)
        a.m();

        Beta b = new @CALyear Beta();
        b.m();
    }
}
