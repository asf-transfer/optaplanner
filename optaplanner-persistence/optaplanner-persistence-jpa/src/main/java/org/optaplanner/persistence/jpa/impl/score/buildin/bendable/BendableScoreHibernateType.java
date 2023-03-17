package org.optaplanner.persistence.jpa.impl.score.buildin.bendable;

import java.util.Properties;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.ParameterizedType;
import org.optaplanner.core.impl.score.buildin.BendableScoreDefinition;
import org.optaplanner.persistence.jpa.impl.score.AbstractScoreHibernateType;

/**
 * @deprecated This class has been deprecated as the Hibernate 6 does not provide full backward compatibility
 * for the {@link CompositeUserType}. The class will remain available in the OptaPlanner 8 releases to provide
 * integration with Hibernate 5 but will be removed in the OptaPlanner 9.37.0.Beta release.
 * To integrate the {@link org.optaplanner.core.api.score.buildin.bendable.BendableScore} with Hibernate 6,
 * either use the score converter
 * {@link org.optaplanner.persistence.jpa.api.score.buildin.bendable.BendableScoreConverter})
 * or implement the {@link CompositeUserType} yourself.
 */
@Deprecated(forRemoval = true)
public class BendableScoreHibernateType extends AbstractScoreHibernateType implements ParameterizedType {

    @Override
    public void setParameterValues(Properties parameterMap) {
        int hardLevelsSize = extractIntParameter(parameterMap, "hardLevelsSize");
        int softLevelsSize = extractIntParameter(parameterMap, "softLevelsSize");
        scoreDefinition = new BendableScoreDefinition(hardLevelsSize, softLevelsSize);
        type = StandardBasicTypes.INTEGER;
    }

}
