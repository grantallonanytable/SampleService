package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.javamoney.moneta.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * "Moneta" adoption of the snippet (https://github.com/JavaMoney/jsr354-ri/issues/185#issuecomment-448661154).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public class PersistentMoneyAmountAndCurrency implements CompositeUserType {
    static final String NAME = "persistentMoneyAmountAndCurrency";

    @Override
    public String[] getPropertyNames() {
        // ORDER IS IMPORTANT!  it must match the order the columns are defined in the property mapping
        return new String[]{"currency", "amount"};
    }

    @Override
    public Type[] getPropertyTypes() {
        return new Type[]{StringType.INSTANCE, BigDecimalType.INSTANCE};
    }

    @Override
    public Class returnedClass() {
        return Money.class;
    }

    @Override
    public Object getPropertyValue(Object component, int propertyIndex) {
        if (component == null) {
            return null;
        }
        Object res;
        final Money money = (Money) component;
        switch (propertyIndex) {
            case 0:
                res = money.getCurrency().getCurrencyCode();
                break;
            case 1:
                res = money.getNumber().numberValue(BigDecimal.class);
                break;
            default:
                throw new HibernateException("Invalid property index [" + propertyIndex + "]");
        }
        return res;
    }

    @Override
    public void setPropertyValue(Object component, int propertyIndex, Object value) {
        if (component == null) {
            return;
        }
        throw new HibernateException("Called setPropertyValue on an immutable type {" + component.getClass() + "}");
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SharedSessionContractImplementor session,
                              Object object) throws SQLException {
        assert names.length == 2;

        //owner here is of type TestUser or the actual owning Object
        Money money = null;
        final String currency = resultSet.getString(names[0]);
        //Deferred check after first read
        if (!resultSet.wasNull()) {
            final BigDecimal amount = resultSet.getBigDecimal(names[1]);
            money = Money.of(amount, currency);
        }
        return money;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int property,
                            SharedSessionContractImplementor session) throws SQLException {
        if (null == value) {
            preparedStatement.setNull(property, StringType.INSTANCE.sqlType());
            preparedStatement.setNull(property + 1, BigDecimalType.INSTANCE.sqlType());
        } else {
            final Money amount = (Money) value;
            preparedStatement.setString(property, amount.getCurrency().getCurrencyCode());
            preparedStatement.setBigDecimal(property + 1, amount.getNumber().numberValue(BigDecimal.class));
        }
    }

    /**
     * Used while dirty checking - control passed on to the {@link javax.money.MonetaryAmount}.
     */
    @Override
    public boolean equals(final Object o1, final Object o2) {
        return Objects.equals(o1, o2);
    }

    @Override
    public int hashCode(final Object value) {
        return value.hashCode();
    }

    /**
     * Helps hibernate apply certain optimizations for immutable objects.
     */
    @Override
    public boolean isMutable() {
        return false;
    }

    /**
     * Used to create Snapshots of the object.
     */
    @Override
    public Object deepCopy(final Object value) {
        return value; //if object was immutable we could return the object as its is
    }

    /**
     * Method called when Hibernate puts the data in a second level cache. The data is stored
     * in a serializable form.
     */
    @Override
    public Serializable disassemble(final Object value,
                                    final SharedSessionContractImplementor paramSessionImplementor) {
        //Thus the data Types must implement serializable
        return (Serializable) value;
    }

    /**
     * Returns the object from the 2 level cache.
     */
    @Override
    public Object assemble(final Serializable cached,
                           final SharedSessionContractImplementor sessionImplementor, final Object owner) {
        //would work as the class is Serializable, and stored in cache as it is - see disassemble
        return cached;
    }

    /**
     * Method is called when merging two objects.
     */
    @Override
    public Object replace(final Object original, final Object target,
                          final SharedSessionContractImplementor paramSessionImplementor, final Object owner) {
        return original; // if immutable use this
    }

}
