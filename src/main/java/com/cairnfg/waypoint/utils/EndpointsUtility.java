
package com.cairnfg.waypoint.utils;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Household;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.enumeration.HouseholdRoleEnum;

public class EndpointsUtility {

    public static HouseholdRoleEnum getHouseholdRole(Household household, Account account) {
        try {
            if (household.getPrimaryContacts().contains(account)) {
                return HouseholdRoleEnum.PRIMARY_CONTACT;
            } else if (account.getCoClient() != null) {
                return HouseholdRoleEnum.CO_CLIENT;
            } else {
                return HouseholdRoleEnum.DEPENDENT;
            }
        } catch (Exception e) {
            return null;
        }
    }
}