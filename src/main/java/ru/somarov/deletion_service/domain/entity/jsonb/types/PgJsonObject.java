package ru.somarov.deletion_service.domain.entity.jsonb.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class is a custom data type for hibernate
 *
 * @author alexandr.omarov, bajura-ea
 * @version 1.0.0
 * @since 1.0.0
 *
 */
public class PgJsonObject implements UserType {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    @Override
    public Class returnedClass() {
        return this.getClass();
    }

    @Override
    public boolean equals(Object o, Object o2) throws HibernateException {
        return o == null ? o2 == null : o.equals(o2);
    }

    @Override
    public int hashCode(Object o) throws HibernateException {
        return o.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        if (rs == null || rs.getObject(names[0]) == null) {
            return null;
        }
        var pGobject = (PGobject) rs.getObject(names[0]);
        Object jsonObject;
        try {
            jsonObject = readJson(pGobject.getValue());
        } catch (IOException e) {
            throw new SQLException(e);
        }
        return jsonObject;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (st == null) {
            return;
        }
        if (value == null) {
            st.setNull(index, Types.NULL);
            return;
        }
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new SQLException(e);
        }
        var pGobject = buildPGObject();
        pGobject.setValue(jsonString);
        st.setObject(index, pGobject);
    }

    protected PGobject buildPGObject() {
        var pGobject = new PGobject();
        pGobject.setType("jsonb");
        return pGobject;
    }

    protected Object readJson(String value) throws IOException {
        return objectMapper.readValue(value, this.returnedClass());
    }

    @Override
    public Object deepCopy(Object o) throws HibernateException {
        Object copy;
        try {
            copy = readJson(objectMapper.writeValueAsString(o));
        } catch (IOException e) {
            throw new HibernateException(e);
        }
        return copy;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object o) throws HibernateException {
        return (Serializable) this.deepCopy(o);
    }

    @Override
    public Object assemble(Serializable serializable, Object o) throws HibernateException {
        return this.deepCopy(serializable);
    }

    @Override
    public Object replace(Object o, Object o2, Object o3) throws HibernateException {
        return this.deepCopy(o);
    }

    
}


