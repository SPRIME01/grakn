/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn.graql.internal.pattern.property;

import ai.grakn.GraknGraph;
import ai.grakn.concept.Concept;
import ai.grakn.concept.RoleType;
import ai.grakn.concept.TypeLabel;
import ai.grakn.graql.Graql;
import ai.grakn.graql.Var;
import ai.grakn.graql.admin.Atomic;
import ai.grakn.graql.admin.ReasonerQuery;
import ai.grakn.graql.admin.VarPatternAdmin;
import ai.grakn.graql.internal.gremlin.EquivalentFragmentSet;
import ai.grakn.graql.internal.gremlin.sets.EquivalentFragmentSets;
import ai.grakn.graql.internal.query.InsertQueryExecutor;
import ai.grakn.graql.internal.reasoner.atom.binary.TypeAtom;
import ai.grakn.graql.internal.reasoner.atom.predicate.IdPredicate;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static ai.grakn.graql.internal.reasoner.ReasonerUtils.getIdPredicate;

/**
 * Reperesents the {@code plays} property on a {@link ai.grakn.concept.Type}.
 *
 * This property relates a {@link ai.grakn.concept.Type} and a {@link RoleType}. It indicates that an
 * {@link ai.grakn.concept.Instance} whose type is this {@link ai.grakn.concept.Type} is permitted to be a role-player
 * playing the role of the given {@link RoleType}.
 *
 * @author Felix Chapman
 */
public class PlaysProperty extends AbstractVarProperty implements NamedProperty {

    private final VarPatternAdmin role;
    private final boolean required;

    public PlaysProperty(VarPatternAdmin role, boolean required) {
        this.role = role;
        this.required = required;
    }

    public VarPatternAdmin getRole() {
        return role;
    }

    @Override
    public String getName() {
        return "plays";
    }

    @Override
    public String getProperty() {
        return role.getPrintableName();
    }

    @Override
    public Collection<EquivalentFragmentSet> match(Var start) {
        return ImmutableSet.of(EquivalentFragmentSets.plays(start, role.getVarName(), required));
    }

    @Override
    public Stream<VarPatternAdmin> getTypes() {
        return Stream.of(role);
    }

    @Override
    public Stream<VarPatternAdmin> getInnerVars() {
        return Stream.of(role);
    }

    @Override
    public void insert(InsertQueryExecutor insertQueryExecutor, Concept concept) throws IllegalStateException {
        RoleType roleType = insertQueryExecutor.getConcept(role).asRoleType();
        concept.asType().plays(roleType);
    }

    @Override
    public void delete(GraknGraph graph, Concept concept) {
        TypeLabel roleLabel = role.getTypeLabel().orElseThrow(() -> failDelete(this));
        concept.asType().deletePlays(graph.getType(roleLabel));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaysProperty that = (PlaysProperty) o;

        return required == that.required && role.equals(that.role);

    }

    @Override
    public int hashCode() {
        int result = role.hashCode();
        result = 31 * result + (required ? 1 : 0);
        return result;
    }

    @Override
    public Atomic mapToAtom(VarPatternAdmin var, Set<VarPatternAdmin> vars, ReasonerQuery parent) {
        Var varName = var.getVarName();
        VarPatternAdmin typeVar = this.getRole();
        Var typeVariable = typeVar.getVarName();
        IdPredicate predicate = getIdPredicate(typeVariable, typeVar, vars, parent);

        VarPatternAdmin resVar = Graql.var(varName).plays(Graql.var(typeVariable)).admin();
        return new TypeAtom(resVar, predicate, parent);
    }
}
