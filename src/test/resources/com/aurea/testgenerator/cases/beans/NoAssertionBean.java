package com.redknee.app.crm.bean;

import com.redknee.app.crm.bean.*;
import com.redknee.app.crm.extension.*;
import com.redknee.app.crm.extension.account.*;
import com.redknee.app.crm.web.control.*;
import com.redknee.framework.xhome.beans.RemoteBeanSupport;
import com.redknee.framework.xhome.context.*;
import com.redknee.framework.xhome.holder.*;
import com.redknee.framework.xhome.webcontrol.*;
import com.redknee.util.snippet.webcontrol.*;

import java.util.List;

/**
 * {{{GENERATED_CODE}}} {{{PLEASE_DO_NO_MODIFY}}}
 **/
public class RemoteAccountCreationTemplate
        extends AbstractBean {

    protected final RemoteBeanSupport support_;


    //////////////////////////////////////////////////////// CONSTRUCTORS

    public RemoteAccountCreationTemplate(Context ctx, String host, int port, String service) {
        support_ = new RemoteBeanSupport(ctx, this, host, port, service);
    }

    public RemoteAccountCreationTemplate(Context ctx, String host, int port, String service, long delay) {
        support_ = new RemoteBeanSupport(ctx, this, host, port, service, delay);
    }


    /////////////////////////////////////////////////////// Property Identifier

    public long getIdentifier() {
        support_.sync();

        return super.getIdentifier();
    }


    /////////////////////////////////////////////////////// Property Name

    public String getName() {
        support_.sync();

        return super.getName();
    }


    /////////////////////////////////////////////////////// Property Spid

    public int getSpid() {
        support_.sync();

        return super.getSpid();
    }


    /////////////////////////////////////////////////////// Property Type

    public long getType() {
        support_.sync();

        return super.getType();
    }


    /////////////////////////////////////////////////////// Property GroupType

    public GroupTypeEnum getGroupType() {
        support_.sync();

        return super.getGroupType();
    }


    /////////////////////////////////////////////////////// Property SystemType

    public SubscriberTypeEnum getSystemType() {
        support_.sync();

        return super.getSystemType();
    }


    /////////////////////////////////////////////////////// Property Responsible

    public boolean getResponsible() {
        support_.sync();

        return super.getResponsible();
    }


    /////////////////////////////////////////////////////// Property CreditCategory

    public int getCreditCategory() {
        support_.sync();

        return super.getCreditCategory();
    }


    /////////////////////////////////////////////////////// Property DealerCode

    public String getDealerCode() {
        support_.sync();

        return super.getDealerCode();
    }


    /////////////////////////////////////////////////////// Property DiscountClass

    public int getDiscountClass() {
        support_.sync();

        return super.getDiscountClass();
    }


    /////////////////////////////////////////////////////// Property TaxAuthority

    public int getTaxAuthority() {
        support_.sync();

        return super.getTaxAuthority();
    }


    /////////////////////////////////////////////////////// Property TaxExemption

    public boolean getTaxExemption() {
        support_.sync();

        return super.getTaxExemption();
    }


    /////////////////////////////////////////////////////// Property Language

    public String getLanguage() {
        support_.sync();

        return super.getLanguage();
    }


    /////////////////////////////////////////////////////// Property BillCycleID

    public int getBillCycleID() {
        support_.sync();

        return super.getBillCycleID();
    }


    /////////////////////////////////////////////////////// Property BillingMsgPreference

    public BillingMessagePreferenceEnum getBillingMsgPreference() {
        support_.sync();

        return super.getBillingMsgPreference();
    }


    /////////////////////////////////////////////////////// Property BillingCountry

    public String getBillingCountry() {
        support_.sync();

        return super.getBillingCountry();
    }


    /////////////////////////////////////////////////////// Property BillingProvince

    public String getBillingProvince() {
        support_.sync();

        return super.getBillingProvince();
    }


    /////////////////////////////////////////////////////// Property PaymentMethodType

    public long getPaymentMethodType() {
        support_.sync();

        return super.getPaymentMethodType();
    }


    /////////////////////////////////////////////////////// Property InvoiceDeliveryOption

    public long getInvoiceDeliveryOption() {
        support_.sync();

        return super.getInvoiceDeliveryOption();
    }


    /////////////////////////////////////////////////////// Property AccountExtensions

    public List getAccountExtensions() {
        support_.sync();

        return super.getAccountExtensions();
    }


    /////////////////////////////////////////////////////// Property MandatoryFields

    public List getMandatoryFields() {
        support_.sync();

        return super.getMandatoryFields();
    }


    public void sync(Object obj) {
        AccountCreationTemplate source = (AccountCreationTemplate) obj;
        identifier_ = source.getIdentifier();
        name_ = source.getName();
        spid_ = source.getSpid();
        type_ = source.getType();
        groupType_ = source.getGroupType();
        systemType_ = source.getSystemType();
        responsible_ = source.getResponsible();
        creditCategory_ = source.getCreditCategory();
        dealerCode_ = source.getDealerCode();
        discountClass_ = source.getDiscountClass();
        taxAuthority_ = source.getTaxAuthority();
        TaxExemption_ = source.getTaxExemption();
        language_ = source.getLanguage();
        billCycleID_ = source.getBillCycleID();
        billingMsgPreference_ = source.getBillingMsgPreference();
        billingCountry_ = source.getBillingCountry();
        billingProvince_ = source.getBillingProvince();
        paymentMethodType_ = source.getPaymentMethodType();
        invoiceDeliveryOption_ = source.getInvoiceDeliveryOption();
        accountExtensions_ = source.getAccountExtensions();
        mandatoryFields_ = source.getMandatoryFields();

    }
}


