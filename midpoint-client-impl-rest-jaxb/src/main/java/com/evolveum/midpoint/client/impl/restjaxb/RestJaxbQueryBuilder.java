/**
 * Copyright (c) 2017 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.client.impl.restjaxb;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.client.api.AtomicFilterExit;
import com.evolveum.midpoint.client.api.ConditionEntryBuilder;
import com.evolveum.midpoint.client.api.MatchingRuleEntryBuilder;
import com.evolveum.midpoint.client.api.QueryBuilder;
import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

/**
 * 
 * @author katkav
 *
 */
public class RestJaxbQueryBuilder<O extends ObjectType> implements QueryBuilder<O>, ConditionEntryBuilder<O>, MatchingRuleEntryBuilder<O> {

	private ItemPathType itemPath;
	private RestJaxbQueryBuilder originalFilter;
	private Element filterClause;

	private RestJaxbService queryForService;
	private Class<O> type;
	
	private QueryType query;

	RestJaxbQueryBuilder(RestJaxbQueryBuilder<O> originalFilter, ItemPathType itemPath) {
		this(originalFilter.queryForService, originalFilter.type);
		this.itemPath = itemPath;
	}

	RestJaxbQueryBuilder(RestJaxbQueryBuilder<O> originalFilter, Element filterClause) {
		this(originalFilter.queryForService, originalFilter.type);
		this.originalFilter = originalFilter;
		this.filterClause = filterClause;
	}
	
	RestJaxbQueryBuilder(RestJaxbService searchService, Class<O> type, QueryType query) {
		this(searchService, type);
		this.query = query;
	}

	public RestJaxbQueryBuilder(RestJaxbService searchService, Class<O> type) {
		this.queryForService = searchService;
		this.type = type;
	}

	@Override
	public SearchService<O> build() {
		return new RestJaxbSearchService<O>(queryForService, type, query);
	}

	public SearchFilterType buildFilter() {
		SearchFilterType filter = new SearchFilterType();
		filter.setFilterClause(filterClause);
		return filter;
	}

	@Override
	public MatchingRuleEntryBuilder<O> eq(Object... values) {
		Element equal = queryForService.getDomSerializer().createEqualFilter(itemPath, Arrays.asList(values));
		return new RestJaxbQueryBuilder<O>(this, equal);
	}

	@Override
	public ConditionEntryBuilder<O> item(ItemPathType itemPath) {
		return new RestJaxbQueryBuilder<O>(this, itemPath);
	}

	@Override
	public ConditionEntryBuilder<O> item(QName... qnames) {
		return new RestJaxbQueryBuilder<>(this, queryForService.util().createItemPathType(qnames));
	}

	@Override
	public MatchingRuleEntryBuilder<O> eq() {
		Element equal = queryForService.getDomSerializer().createEqualFilter(itemPath, null);
		return new RestJaxbQueryBuilder<O>(this, equal);
	}

	@Override
	public MatchingRuleEntryBuilder<O> eqPoly(String orig, String norm) {
		Element equal = queryForService.getDomSerializer().createEqualPolyFilter(itemPath, orig, norm);
		return new RestJaxbQueryBuilder<O>(this, equal);
	}

	@Override
	public MatchingRuleEntryBuilder<O> eqPoly(String orig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> gt(Object value) {
		return null;
	}

	@Override
	public QueryBuilder<O> gt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> ge(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> ge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> lt(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> lt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> le(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> le() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> startsWith(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> startsWithPoly(String orig, String norm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> startsWithPoly(String orig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> endsWith(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> endsWithPoly(String orig, String norm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> endsWithPoly(String orig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> contains(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> containsPoly(String orig, String norm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntryBuilder<O> containsPoly(String orig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> ref(QName relation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> ref(ObjectReferenceType... value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> ref(Collection<ObjectReferenceType> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> ref(String... oid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> ref(String oid, QName targetTypeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> isNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> and() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> or() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryBuilder<O> finishQuery() {
		QueryType query = new QueryType();
		query.setFilter(buildFilter());;
		
		return new RestJaxbQueryBuilder<>(queryForService, type, query);
	}

	@Override
	public AtomicFilterExit<O> matchingOrig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AtomicFilterExit<O> matchingNorm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AtomicFilterExit<O> matchingStrict() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AtomicFilterExit<O> matchingCaseIgnore() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AtomicFilterExit<O> matching(QName matchingRuleName) {
		// TODO Auto-generated method stub
		return null;
	}

}
