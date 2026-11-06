package com.thomasvitale.mousike.kontakt.instrument;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Read-only access to Kontakt's {@code v_sound_info} view of its Browser presets and its FTS5 index {@code f_sound_info}.
 * Derived queries cover the view. The full-text search and the catalogue summaries are SQL,
 * mapped onto records by Spring Data.
 */
public interface InstrumentRepository extends ListCrudRepository<Instrument, Long> {

    int DEFAULT_LIMIT = 12;
    int MAX_LIMIT = 50;

    /**
     * Full-text search. Every word must match first.
     * When nothing does, any word may match, so "warm brass" still finds brass.
     */
    default List<Instrument> search(String query, @Nullable String soundType, @Nullable String character, int limit) {
        return search(query, soundType, character, null, null, limit);
    }

    /**
     * Full-text search. Every word must match first.
     * When nothing does, any word may match, so "warm brass" still finds brass.
     * Without a category, Tools are left out: their presets make no sound.
     */
    default List<Instrument> search(String query, @Nullable String soundType, @Nullable String character,
            @Nullable PresetCategory category, @Nullable Collection<String> products, int limit) {
        List<String> terms = terms(query);
        if (terms.isEmpty()) {
            return List.of();
        }
        int size = Math.clamp(limit, 1, MAX_LIMIT);
        List<String> extensions = category != null ? category.extensions() : Arrays.stream(PresetCategory.values())
                .filter(each -> each != PresetCategory.TOOLS)
                .flatMap(each -> each.extensions().stream())
                .toList();
        boolean anyProduct = products == null;
        Collection<String> within = anyProduct ? List.of() : products;
        List<Instrument> all = match(String.join(" ", terms), like(soundType), like(character), extensions, anyProduct, within, size);
        if (!all.isEmpty() || terms.size() == 1) {
            return all;
        }
        return match(String.join(" OR ", terms), like(soundType), like(character), extensions, anyProduct, within, size);
    }

    /**
     * The view has no rowid, so the FTS index joins on {@code id}, its content rowid.
     */
    @Query("""
            select s.*
            from f_sound_info f join v_sound_info s on s.id = f.rowid
            where f_sound_info match :match
              and (:soundTypeLike is null or lower(coalesce(s.type, '')) like :soundTypeLike)
              and (:characterLike is null or lower(coalesce(s.character, '')) like :characterLike)
              and s.file_ext in (:extensions)
              and (:anyProduct or s.product in (:products))
            order by rank
            limit :limit""")
    List<Instrument> match(String match, @Nullable String soundTypeLike, @Nullable String characterLike,
            Collection<String> extensions, boolean anyProduct, Collection<String> products, int limit);

    /**
     * The Sound Types of each preset, one Type and Subtype pair per row.
     */
    @Query("""
            select sc.sound_info_id as instrument, c.category || coalesce(', ' || c.subcategory, '') as name
            from k_sound_info_category sc join k_category c on c.id = sc.category_id
            where sc.sound_info_id in (:ids)
            order by name""")
    List<InstrumentTag> soundTypes(Collection<Long> ids);

    /**
     * The Character tags of each preset, one per row.
     */
    @Query("""
            select sm.sound_info_id as instrument, m.name as name
            from k_sound_info_mode sm join k_mode m on m.id = sm.mode_id
            where sm.sound_info_id in (:ids)
            order by name""")
    List<InstrumentTag> characters(Collection<Long> ids);

    List<Instrument> findByProductOrderByName(String product);

    /**
     * Each product lives in one content path, which tells where Kontakt found it
     * and which version is installed.
     */
    @Query("""
            select v.product, coalesce(nullif(min(v.author), ''), min(v.brand)) as author, count(*) as presets,
                   min(cp.product_version_s) as version, min(cp.path) as folder
            from v_sound_info v
              join k_sound_info s on s.id = v.id
              left join k_content_path cp on cp.id = s.content_path_id
            group by v.product
            order by presets desc, v.product""")
    List<Library> libraries();

    /**
     * The Sound Types, Type and Subtype pairs as the Browser's Sound Type filter nests them,
     * most used first.
     */
    @Query("""
            select c.category || coalesce(', ' || c.subcategory, '') as name, count(distinct sc.sound_info_id) as presets
            from k_category c join k_sound_info_category sc on sc.category_id = c.id
            group by name
            order by presets desc, name""")
    List<Tag> soundTypes();

    /**
     * The Character tags, most used first.
     */
    @Query("""
            select m.name as name, count(*) as presets
            from k_mode m join k_sound_info_mode sm on sm.mode_id = m.id
            group by m.name
            order by presets desc, name""")
    List<Tag> characters();

    @Query("""
            select (select count(*) from v_sound_info) as presets,
                   (select count(distinct product) from v_sound_info) as libraries,
                   (select count(distinct category_id) from k_sound_info_category) as sound_types,
                   (select count(distinct mode_id) from k_sound_info_mode) as characters""")
    InstrumentSummary summary();

    /**
     * Words of the query as FTS5 prefix terms, quoted so punctuation cannot change the query's meaning.
     */
    static List<String> terms(String query) {
        return Arrays.stream(query.split("[^\\p{L}\\p{N}#]+"))
                .filter(term -> !term.isBlank())
                .map(term -> "\"" + term + "\"*")
                .toList();
    }

    private static @Nullable String like(@Nullable String filter) {
        return filter == null || filter.isBlank() ? null : "%" + filter.strip().toLowerCase(Locale.ROOT) + "%";
    }

}
