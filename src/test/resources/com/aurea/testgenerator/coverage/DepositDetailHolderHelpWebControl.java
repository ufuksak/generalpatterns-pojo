package com.redknee.app.crm.bean;

/*
    DepositDetailHolderHelpWebControl

    Author : Kevin Greer (via xgen)
    Date   : Thu Jan 25 22:42:44 MSK 2018

    Copyright (c) Redknee Inc., 2006
        - all rights reserved
*/


import com.redknee.app.crm.bean.*;
import com.redknee.framework.xhome.beans.*;
import com.redknee.framework.xhome.beans.xi.XInfo;
import com.redknee.framework.xhome.context.*;
import com.redknee.framework.xhome.language.*;
import com.redknee.framework.xhome.support.*;
import com.redknee.framework.xhome.web.action.*;
import com.redknee.framework.xhome.web.renderer.HelpRenderer;
import com.redknee.framework.xhome.web.support.*;
import com.redknee.framework.xhome.web.trace.WebTrace;
import com.redknee.framework.xhome.webcontrol.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.*;
import java.io.PrintWriter;

/**
 * {{{GENERATED_CODE}}} {{{PLEASE_DO_NO_MODIFY}}}
 */
public class DepositDetailHolderHelpWebControl
        extends AbstractWebControl
        implements HelpWebControl {

    protected final static HelpWebControl instance__ = new DepositDetailHolderHelpWebControl();

    //Intentional making the following static field "non-final" to enable the ability to change dynamically later.
    protected static XInfo xInfo_ = DepositDetailHolderXInfo.instance();


    public DepositDetailHolderHelpWebControl() {
    }


    ////////////////////////////////////////////////////////// CONSTRUCTOR

    public static HelpWebControl instance() {
        return instance__;
    }


    /////////////////////////////////////////////////////////////////////////
    //                                                         METHODS
    /////////////////////////////////////////////////////////////////////////

    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name) {
        throw new UnsupportedOperationException("fromWeb() not supported for HelpWebControl types");
    }


    public Object fromWeb(Context ctx, ServletRequest req, String name) {
        throw new UnsupportedOperationException("fromWeb() not supported for HelpWebControl types");
    }

    @SuppressWarnings(value = {"DLS_DEAD_LOCAL_STORE"}, justification = "Variable subCtx is used some of the time.")
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj) {
        if (WebTrace.TRACE_ENABLED) {
            WebTrace.begin(ctx, out, "DepositDetailHolderHelpWebControl (WebControl.xsl)");
        }

        HelpRenderer renderer = helpRenderer(ctx);
        MessageMgr mmgr = new MessageMgr(ctx, this);


        /*********  Entity-Level label and Help   **************/
        StringBuilder table_title_text = new StringBuilder(
                DepositDetailHolderXInfo.instance().getLabel(ctx));


        //table_title_text.append("<br/><font>");
        String help = DepositDetailHolderXInfo.instance().getHelp(ctx).trim();
        //table_title_text.append(help);
        if (help == null || help.length() == 0) {
            //table_title_text.append(help.length() == 0 ? "" : "<br/>");
            help = "<br/>";
        }
        //table_title_text.append("</font>");

        renderer.Help(out, table_title_text.toString(), help);

        /*********  Property-Level label and Help  *************/

        ViewModeEnum mode;

        renderer.FieldList(out);

        ///////////////
        Context subCtx = ctx.createSubContext();


        //////////////


        mode = getMode(subCtx, "DepositDetailHolder.correlationID");

        if (mode != ViewModeEnum.NONE) {

            renderer.FieldTitle(
                    out,
                    DepositDetailHolderXInfo.CORRELATION_ID.getLabel(ctx));

            renderer.FieldHelp(
                    out,
                    DepositDetailHolderXInfo.CORRELATION_ID.getHelp(ctx),
                    DepositDetailHolderXInfo.CORRELATION_ID,
                    "correlationID");

            //  added for low-level bean's help description

            // end of low-level bean help description

            renderer.FieldEnd(out);

        } // if mode


        mode = getMode(subCtx, "DepositDetailHolder.depositDetails");

        if (mode != ViewModeEnum.NONE) {

            renderer.FieldTitle(
                    out,
                    DepositDetailHolderXInfo.DEPOSIT_DETAILS.getLabel(ctx));

            renderer.FieldHelp(
                    out,
                    DepositDetailHolderXInfo.DEPOSIT_DETAILS.getHelp(ctx),
                    DepositDetailHolderXInfo.DEPOSIT_DETAILS,
                    "depositDetails");

            //  added for low-level bean's help description

            // TODO: Add Enum  Help
            DepositDetailHelpWebControl.instance().toWeb(subCtx, out, "", null);

            // end of low-level bean help description

            renderer.FieldEnd(out);

        } // if mode


        renderer.FieldListEnd(out);

        renderer.HelpEnd(out);

        if (WebTrace.TRACE_ENABLED) {
            WebTrace.end(ctx, out, "DepositDetailHolderHelpWebControl (WebControl.xsl)");
        }
    }


}

