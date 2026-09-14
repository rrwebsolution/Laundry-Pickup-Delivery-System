package view.components;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;
import org.kordamp.ikonli.swing.FontIcon;

import java.awt.Color;

/**
 * Single source of truth for every icon used in the UI, backed by Ikonli's Material Design 2
 * icon pack (real vector glyphs rendered through an embedded font, not emoji/Unicode symbols).
 * Java Swing renders these reliably regardless of OS emoji-font support.
 */
public final class AppIcon {

    public enum Name {
        DASHBOARD(MaterialDesignV.VIEW_DASHBOARD),
        CUSTOMERS(MaterialDesignA.ACCOUNT_GROUP),
        SERVICES(MaterialDesignW.WASHING_MACHINE),
        ORDERS(MaterialDesignC.CLIPBOARD_LIST),
        PICKUP(MaterialDesignT.TRUCK),
        DELIVERY(MaterialDesignT.TRUCK_DELIVERY),
        PICKUP_DELIVERY(MaterialDesignT.TRUCK_DELIVERY),
        PAYMENTS(MaterialDesignC.CREDIT_CARD),
        REPORTS(MaterialDesignC.CHART_BAR),
        USERS(MaterialDesignS.SHIELD_ACCOUNT),
        SETTINGS(MaterialDesignC.COG),
        LOGOUT(MaterialDesignL.LOGOUT),

        SEARCH(MaterialDesignM.MAGNIFY),
        ADD(MaterialDesignP.PLUS),
        EDIT(MaterialDesignP.PENCIL),
        DELETE(MaterialDesignT.TRASH_CAN),
        SAVE(MaterialDesignC.CONTENT_SAVE),
        CANCEL(MaterialDesignC.CLOSE),
        VIEW(MaterialDesignE.EYE),
        REFRESH(MaterialDesignR.REFRESH),

        NOTIFICATION(MaterialDesignB.BELL),
        USERNAME(MaterialDesignA.ACCOUNT),
        PASSWORD(MaterialDesignL.LOCK),
        CALENDAR(MaterialDesignC.CALENDAR),
        LOCATION(MaterialDesignM.MAP_MARKER),
        RIDER(MaterialDesignM.MOTORBIKE),
        CHEVRON_DOWN(MaterialDesignC.CHEVRON_DOWN),

        CHECK(MaterialDesignC.CHECK),
        CHECK_CIRCLE(MaterialDesignC.CHECK_CIRCLE),
        CIRCLE(MaterialDesignC.CIRCLE),
        CIRCLE_OUTLINE(MaterialDesignC.CIRCLE_OUTLINE),
        WARNING(MaterialDesignA.ALERT),
        ERROR(MaterialDesignC.CLOSE_CIRCLE),
        CLOCK(MaterialDesignC.CLOCK_OUTLINE),
        MONEY(MaterialDesignC.CURRENCY_PHP);

        final Ikon ikon;

        Name(Ikon ikon) {
            this.ikon = ikon;
        }
    }

    private AppIcon() {
    }

    public static FontIcon of(Name name, int size, Color color) {
        return FontIcon.of(name.ikon, size, color);
    }
}
