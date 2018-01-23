package com.evolveum.midpoint.client.impl.restjaxb;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.client.api.AtomicFilterExit;
import com.evolveum.midpoint.client.api.ConditionEntryBuilder;
import com.evolveum.midpoint.client.api.QueryBuilder;
import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.query_3.FilterClauseType;
import com.evolveum.prism.xml.ns._public.query_3.NAryLogicalOperatorFilterClauseType;
import com.evolveum.prism.xml.ns._public.query_3.OrgFilterClauseType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

public class FilterBuilder<O extends ObjectType> implements QueryBuilder<O>, AtomicFilterExit<O>{
	
	private NAryLogicalOperatorFilterClauseType currentFilter;
	private FilterLogicalSymbol lastLogicalSymbol;
	
	private RestJaxbService service;
	private Class<O> type;
	
	public FilterBuilder(RestJaxbService service, Class<O> type) {
		this.service = service;
		this.type = type;
		this.currentFilter = new NAryLogicalOperatorFilterClauseType();
		lastLogicalSymbol = null;
			
	}
	
	private FilterBuilder(RestJaxbService service, Class<O> type, NAryLogicalOperatorFilterClauseType currentFilter, FilterLogicalSymbol logicalSymbol) {
		this.service = service;
		this.type = type;
		this.currentFilter = currentFilter;
		this.lastLogicalSymbol = logicalSymbol;
	}

	
	public FilterBuilder<O> addSubfilter(Element subfilter, boolean negated) {
        if (!currentFilter.getFilterClause().isEmpty() && lastLogicalSymbol == null) {
            throw new IllegalStateException("lastLogicalSymbol is empty but there is already some filter present: " + currentFilter);
        }
            NAryLogicalOperatorFilterClauseType newFilter = appendAtomicFilter(subfilter, negated, lastLogicalSymbol);
            return new FilterBuilder<>(service, type, newFilter, null);
    }
	
	private NAryLogicalOperatorFilterClauseType appendAtomicFilter(Element subfilter, boolean negated, FilterLogicalSymbol lastLogicalSymbol) {
		 DomSerializer dom = service.getDomSerializer(); 
		if (negated) {
			subfilter = dom.createNotFilter(subfilter);
//	            subfilter = null;// TODO: dom.createNotFilter()
	        }
		 
		 NAryLogicalOperatorFilterClauseType updatedFilter = new NAryLogicalOperatorFilterClauseType();
		 updatedFilter.getFilterClause().addAll(currentFilter.getFilterClause());
		 updatedFilter.setMatching(currentFilter.getMatching());
		 
		
		 
	        if (lastLogicalSymbol == null || lastLogicalSymbol == FilterLogicalSymbol.OR) {
	        	updatedFilter.getFilterClause().add(dom.createAndFilter(subfilter));
	        } else if (lastLogicalSymbol == FilterLogicalSymbol.AND) {
	            Element andFilter = (getLastCondition(updatedFilter));
	            dom.addCondition(andFilter, subfilter);
	        } else {
	            throw new IllegalStateException("Unknown logical symbol: " + lastLogicalSymbol);
	        }
	        return updatedFilter;
		
		
	}
	
	public Element getLastCondition(NAryLogicalOperatorFilterClauseType updatedFilter) {
		List<Element> conditions = updatedFilter.getFilterClause();
		if (conditions.isEmpty()) {
			return null;
		} else {
			return conditions.get(conditions.size()-1);
		}
	}


	@Override
	public SearchService<O> build() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ConditionEntryBuilder<O> item(ItemPathType itemPath) {
		return RestJaxbQueryBuilder.create(service, type, this).item(itemPath);
	}


	@Override
	public ConditionEntryBuilder<O> item(QName... qnames) {
		return RestJaxbQueryBuilder.create(service, type, this).item(qnames);
	}


	@Override
	public QueryBuilder<O> and() {
		return setLastLogicalSymbol(FilterLogicalSymbol.AND);
	}

	@Override
	public QueryBuilder<O> or() {
		return setLastLogicalSymbol(FilterLogicalSymbol.OR);
	}

	private FilterBuilder<O> setLastLogicalSymbol(FilterLogicalSymbol newLogicalSymbol) {
		if (this.lastLogicalSymbol != null) {
            throw new IllegalStateException("Two logical symbols in a sequence");
        }
        return new FilterBuilder<O>(service, type, currentFilter, newLogicalSymbol);
	}


	@Override
	public QueryBuilder<O> finishQuery() {
		QueryType queryType = new QueryType();
		queryType.setFilter(buildFilter());
		return new RestJaxbQueryBuilder<>(service, type, queryType);
	}
	
	public SearchFilterType buildFilter() {
		SearchFilterType filter = new SearchFilterType();
		if (currentFilter.getFilterClause().size() == 1) {
			Element firstFilter = currentFilter.getFilterClause().iterator().next();
			if (firstFilter.getTagName().equals("and")) {
				if (firstFilter.getChildNodes() != null && firstFilter.getChildNodes().getLength() == 1) {
					filter.setFilterClause((Element) firstFilter.getFirstChild());
				} else {
					filter.setFilterClause(firstFilter);
				}
			} else {
				filter.setFilterClause(firstFilter);
			}
		} else {
			Element orFilter = service.getDomSerializer().createOrFilter(currentFilter.getFilterClause());
			filter.setFilterClause(orFilter);
		}
		return filter;
	}

}
