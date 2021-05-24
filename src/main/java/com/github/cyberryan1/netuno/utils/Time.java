package com.github.cyberryan1.netuno.utils;

import java.sql.Timestamp;
import java.time.Duration;

public class Time {

    public static long getCurrentTimestamp() { return System.currentTimeMillis() / 1000; }

    public static String getDateFromTimestamp( long stamp ) {
        Timestamp date = new Timestamp( stamp * 1000 );
        return date.toGMTString();
    }

    // gets the timestamp of a length
    // returns -1 if the length is forever
    public static long getTimestampFromLength( String len ) {
        if ( len.equalsIgnoreCase( "forever" ) ) { return -1; }

        Duration dur = Duration.ZERO;
        char unit = len.charAt( len.length() - 1 );
        int amount = Integer.parseInt( len.substring( 0, len.length() - 1 ) );

        if ( unit == 'w' ) { dur = Duration.ofDays( 7L * amount ); }
        else if ( unit == 'd' ) { dur = Duration.ofDays( ( long ) amount ); }
        else if ( unit == 'h' ) { dur = Duration.ofHours( ( long ) amount ); }
        else if ( unit == 'm' ) { dur = Duration.ofHours( ( long ) amount ); }
        else { dur = Duration.ofSeconds( ( long ) amount ); }

        return dur.getSeconds();
    }

    // supported units: s, m, h, d, w, and forever
    public static String getFormattedLength( String len ) {
        if ( len.equalsIgnoreCase( "forever" ) ) { return "Forever"; }

        int amount = Integer.parseInt( len.substring( 0, len.length() - 1 ) );
        char unit = len.charAt( len.length() - 1 );
        String betterUnit;

        if ( unit == 'w' ) { betterUnit = "week"; }
        else if ( unit == 'd' ) { betterUnit = "day"; }
        else if ( unit == 'h' ) { betterUnit = "hour"; }
        else if ( unit == 'm' ) { betterUnit = "month"; }
        else { betterUnit = "second"; }

        if ( amount > 1 ) {
            betterUnit += "s";
        }

        return amount + " " + betterUnit;
    }

    public static boolean isAllowableLength( String len ) {
        if ( len.equalsIgnoreCase( "forever" ) ) { return true; }

        char unit = len.charAt( len.length() - 1 );
        if ( unit != 'w' && unit != 'd' && unit != 'h' && unit != 'm' && unit != 's' ) { return false; }

        int amount;
        try {
            amount = Integer.parseInt( len.substring( 0, len.length() - 1 ) );
        } catch ( NumberFormatException ex ) {
            return false;
        }

        if ( amount <= 0 ) { return false; }

        return true;
    }

    public static String getLengthFromTimestamp( long len ) {
        if ( len == -1 ) { return "Forever"; }

        int min = ( int ) ( len / 60 );
        int hour = min / 60;
        int day = hour / 24;
        int week =  day / 7;

        if ( min == 0 ) { return getFormattedLength( len + "s" ); }
        else if ( hour == 0 ) { return getFormattedLength( min + "m" ); }
        else if ( day == 0 ) { return getFormattedLength( hour + "h" ); }
        else if ( week == 0 ) { return getFormattedLength( day + "d" ); }
        else { return getFormattedLength( week + "w" ); }
    }
}
