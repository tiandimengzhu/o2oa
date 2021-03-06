package com.x.organization.assemble.express.factory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.x.base.core.project.exception.ExceptionWhen;
import com.x.base.core.project.tools.ListTools;
import com.x.organization.assemble.express.AbstractFactory;
import com.x.organization.assemble.express.Business;
import com.x.organization.assemble.express.CacheFactory;
import com.x.organization.core.entity.Identity;
import com.x.organization.core.entity.Identity_;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public class IdentityFactory extends AbstractFactory {

	private Ehcache cache;

	public IdentityFactory(Business business) throws Exception {
		super(business);
		this.cache = CacheFactory.getIdentityCache();
	}

	public Identity pick(String flag) throws Exception {
		if (StringUtils.isEmpty(flag)) {
			return null;
		}
		Identity o = null;
		Element element = cache.get(flag);
		if (null != element) {
			if (null != element.getObjectValue()) {
				o = (Identity) element.getObjectValue();
			}
		} else {
			o = this.pickObject(flag);
			cache.put(new Element(flag, o));
		}
		return o;
	}

	private Identity pickObject(String flag) throws Exception {
		Identity o = this.entityManagerContainer().flag(flag, Identity.class);
		if (o != null) {
			this.entityManagerContainer().get(Identity.class).detach(o);
		} else {
			String name = flag;
			Matcher matcher = identity_distinguishedName_pattern.matcher(flag);
			if (matcher.find()) {
				name = matcher.group(1);
				String unique = matcher.group(2);
				o = this.entityManagerContainer().flag(unique, Identity.class);
				if (null != o) {
					this.entityManagerContainer().get(Identity.class).detach(o);
				}
			}
			if (null == o) {
				EntityManager em = this.entityManagerContainer().get(Identity.class);
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<Identity> cq = cb.createQuery(Identity.class);
				Root<Identity> root = cq.from(Identity.class);
				Predicate p = cb.equal(root.get(Identity_.name), name);
				List<Identity> os = em.createQuery(cq.select(root).where(p).distinct(true)).getResultList();
				if (os.size() == 1) {
					o = os.get(0);
					em.detach(o);
				}
			}
		}
		return o;
	}

	public List<Identity> pick(List<String> flags) throws Exception {
		List<Identity> list = new ArrayList<>();
		for (String str : flags) {
			Element element = cache.get(str);
			if (null != element) {
				if (null != element.getObjectValue()) {
					list.add((Identity) element.getObjectValue());
				}
			} else {
				Identity o = this.pickObject(str);
				cache.put(new Element(str, o));
				if (null != o) {
					list.add(o);
				}
			}
		}
		return list;
	}

	public <T extends Identity> List<T> sort(List<T> list) {
		list = list.stream().sorted(
				Comparator.comparing(Identity::getOrderNumber, Comparator.nullsLast(Integer::compareTo)).thenComparing(
						Comparator.comparing(Identity::getName, Comparator.nullsFirst(String::compareTo)).reversed()))
				.collect(Collectors.toList());
		return list;
	}

	public List<String> listIdentityDistinguishedNameSorted(List<String> identityIds) throws Exception {
		List<Identity> list = this.entityManagerContainer().list(Identity.class, identityIds);
		list = this.sort(list);
		List<String> values = ListTools.extractProperty(list, "distinguishedName", String.class, true, true);
		return values;
	}

}
