package com.x.program.center.jaxrs.qiyeweixin;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.config.Dingding;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WrapBoolean;
import com.x.program.center.ThisApplication;

class ActionRequestPullSync extends BaseAction {

	ActionResult<Wo> execute(EffectivePerson effectivePerson, JsonElement jsonElement) throws Exception {
		ActionResult<Wo> result = new ActionResult<>();
		if (StringUtils.equals(Dingding.SYNCORGANIZATIONDIRECTION_PULL,
				Config.dingding().getSyncOrganizationDirection())) {
			ThisApplication.qiyeweixinSyncOrganizationCallbackRequest.add(new Object());
		} else {
			throw new ExceptionNotPullSync();
		}
		Wo wo = new Wo();
		wo.setValue(true);
		result.setData(wo);
		return result;
	}

	public static class Wo extends WrapBoolean {
	}

}
