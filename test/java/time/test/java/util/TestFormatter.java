/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package test.java.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import java.util.*;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/* @test
 * @summary Unit test for j.u.Formatter threeten date/time support
 */
@Test
public class TestFormatter {

    // time
    private static String[] fmtStrTime = new String[] {
         "H:[%tH] I:[%1$tI] k:[%1$tk] l:[%1$tl] M:[%1$tM] S:[%1$tS] L:[%1$tL] N:[%1$tN] p:[%1$tp]",
         "H:[%TH] I:[%1$TI] k:[%1$Tk] l:[%1$Tl] M:[%1$TM] S:[%1$TS] L:[%1$TL] N:[%1$TN] p:[%1$Tp]",
         "R:[%tR] T:[%1$tT] r:[%1$tr]",
         "R:[%TR] T:[%1$TT] r:[%1$Tr]"
    };
    // date
    private static String[] fmtStrDate = new String[] {
        "B:[%tB] b:[%1$tb] h:[%1$th] A:[%1$tA] a:[%1$ta] C:[%1$tC] Y:[%1$tY] y:[%1$ty] j:[%1$tj] m:[%1$tm] d:[%1$td] e:[%1$te]",
        "B:[%TB] b:[%1$Tb] h:[%1$Th] A:[%1$TA] a:[%1$Ta] C:[%1$TC] Y:[%1$TY] y:[%1$Ty] j:[%1$Tj] m:[%1$Tm] d:[%1$Td] e:[%1$Te]",
        "D:[%tD] F:[%1$tF]",
        "D:[%TD] F:[%1$TF]"
    };

    private int total = 0;
    private int failure = 0;
    private boolean verbose = true;

    @Test
    public void test () {

        int N = 12;
        //locales = Locale.getAvailableLocales();
        Locale[] locales = new Locale[] {
           Locale.ENGLISH, Locale.FRENCH, Locale.JAPANESE, Locale.CHINESE};

        Random r = new Random();
        ZonedDateTime  zdt = ZonedDateTime.now();
        while (N-- > 0) {
            zdt = zdt.withDayOfYear(r.nextInt(365) + 1)
                     .with(ChronoField.SECOND_OF_DAY, r.nextInt(86400));
            Instant instant = zdt.toInstant();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(instant.toEpochMilli());

            for (Locale locale : locales) {
                    for (String fmtStr : fmtStrDate) {
                    testDate(fmtStr, locale, zdt, cal);
                }
                for (String fmtStr : fmtStrTime) {
                    testTime(fmtStr, locale, zdt, cal);
                }
                testZoneId(locale, zdt, cal);
                testInstant(locale, instant, zdt, cal);
            }
        }
        if (verbose) {
            if (failure != 0) {
                System.out.println("Total " + failure + "/" + total + " tests failed");
            } else {
                System.out.println("All tests (" + total + ") PASSED");
            }
        }
        assertEquals(failure, 0);
    }

    private String getClassName(Object o) {
        Class c = o.getClass();
        return c.getName().substring(c.getPackage().getName().length() + 1);
    }

    private String test(String fmtStr, Locale locale,
                               String expected, Object dt) {
        String out = new Formatter(
            new StringBuilder(), locale).format(fmtStr, dt).out().toString();
        if (verbose) {
            System.out.printf("%-18s  : %s%n", getClassName(dt), out);
        }
        if (expected != null && !out.equals(expected)) {
            System.out.printf("=====>%-18s  : %s  [ FAILED expected: %s ]%n",
                              getClassName(dt), out, expected);
            new RuntimeException().printStackTrace();
            failure++;
        }
        total++;
        return out;
    }

    private void printFmtStr(Locale locale, String fmtStr) {
        if (verbose) {
            System.out.println("--------------------");
            System.out.printf("[%s, %s]%n", locale.toString(), fmtStr);
        }
    }

    private void testDate(String fmtStr, Locale locale,
                                 ZonedDateTime zdt, Calendar cal) {
        printFmtStr(locale, fmtStr);
        String expected = test(fmtStr, locale, null, cal);
        test(fmtStr, locale, expected, zdt);
        test(fmtStr, locale, expected, zdt.toOffsetDateTime());
        test(fmtStr, locale, expected, zdt.toLocalDateTime());
        test(fmtStr, locale, expected, zdt.toLocalDate());
    }

    private void testTime(String fmtStr, Locale locale,
                                 ZonedDateTime zdt, Calendar cal) {
        printFmtStr(locale, fmtStr);
        String expected = test(fmtStr, locale, null, cal);
        test(fmtStr, locale, expected, zdt);
        test(fmtStr, locale, expected, zdt.toOffsetDateTime());
        test(fmtStr, locale, expected, zdt.toLocalDateTime());
        test(fmtStr, locale, expected, zdt.toOffsetDateTime().toOffsetTime());
        test(fmtStr, locale, expected, zdt.toLocalTime());
    }

    private String toZoneIdStr(String expected) {
        return expected.replaceAll("(?:GMT|UTC)(?<off>[+\\-]?[0-9]{2}:[0-9]{2})", "${off}")
                       .replaceAll("GMT|UTC|UT", "Z");
    }

    private void testZoneId(Locale locale, ZonedDateTime zdt, Calendar cal) {
        String fmtStr = "z:[%tz] z:[%1$Tz] Z:[%1$tZ] Z:[%1$TZ]";
        printFmtStr(locale, fmtStr);
        String expected = toZoneIdStr(test(fmtStr, locale, null, cal));
        test(fmtStr, locale, expected, zdt);
        // get a new cal with fixed tz
        Calendar cal0 = Calendar.getInstance();
        cal0.setTimeInMillis(zdt.toInstant().toEpochMilli());
        cal0.setTimeZone(TimeZone.getTimeZone("GMT" + zdt.getOffset().getId()));
        expected = toZoneIdStr(test(fmtStr, locale, null, cal0));
        test(fmtStr, locale, expected, zdt.toOffsetDateTime());
        test(fmtStr, locale, expected, zdt.toOffsetDateTime().toOffsetTime());

        // datetime + zid
        fmtStr = "c:[%tc] c:[%1$Tc]";
        printFmtStr(locale, fmtStr);
        expected = toZoneIdStr(test(fmtStr, locale, null, cal));
        test(fmtStr, locale, expected, zdt);
    }

    private void testInstant(Locale locale, Instant instant,
                             ZonedDateTime zdt, Calendar cal) {
        String fmtStr = "s:[%ts] s:[%1$Ts] Q:[%1$tQ] Q:[%1$TQ]";
        printFmtStr(locale, fmtStr);
        String expected = test(fmtStr, locale, null, cal);
        test(fmtStr, locale, expected, instant);
        test(fmtStr, locale, expected, zdt);
        test(fmtStr, locale, expected, zdt.toOffsetDateTime());
    }
}