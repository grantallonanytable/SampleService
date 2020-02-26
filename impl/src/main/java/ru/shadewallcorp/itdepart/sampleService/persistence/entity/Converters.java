package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import ru.shadewallcorp.itdepart.sampleService.elastic.Money;
import ru.shadewallcorp.itdepart.sampleService.topic.InitOrder;

import java.math.BigDecimal;
import java.util.Collection;
import javax.money.MonetaryAmount;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Конвертеры данных для персистентной сущности.
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public class Converters {
    /**
     * Конвертер классов из сообщений Kafka в классы данных персистентной сущности.
     */
    public static class KafkaToCassandra {
        /**
         * Конвертировать заказ Kafka в заказ персистентной сущности.
         *
         * @param source заказ Kafka
         * @return заказ персистентной сущности.
         */
        @NotNull
        public static Order toOrder(@NotNull InitOrder source) {
            return Order.builder()
                    .id(source.getId())
                    .creationDate(source.getCreationDate())
                    .state(source.getState())
                    .type(source.getType())
                    .siteId(source.getSiteId())
                    .totalPrice(source.getTotalPrice())
                    .payment(source.getPayment() != null ? toPayment(source.getPayment()) : null)
                    .comment(source.getComment())
                    .mnp(source.getMnp())
                    .items(toOrderComposition(source.getItems()))
                    .promo(source.getPromo())
                    .parentNetwork(source.getParentNetwork())
                    .network(source.getNetwork())
                    .partner(source.getPartner())
                    .cancelReason(source.getCancelReason())
                    .delivery(source.getDelivery() != null ? toDelivery(source.getDelivery()) : null)
                    .client(source.getClient() != null ? toClient(source.getClient()) : null)
                    .build();
        }

        /**
         * Конвертировать список строк заказа Kafka в список строк заказа персистентной сущности.
         *
         * @param source исходный список (@Nullable)
         * @return список
         */
        @NotNull
        private static PSequence<OrderCompositionItem> toOrderComposition(
                @Nullable PSequence<ru.shadewallcorp.itdepart.sampleService.topic.OrderCompositionItem> source) {
            return ofNullable(source)
                    .map(Collection::stream)
                    .map(stream -> stream.map(KafkaToCassandra::toOrderCompositionItem).collect(toList()))
                    .map(TreePVector::from)
                    .orElse(TreePVector.empty());
        }

        @NotNull
        private static OrderCompositionItem toOrderCompositionItem(
                ru.shadewallcorp.itdepart.sampleService.topic.OrderCompositionItem source) {
            return OrderCompositionItem.builder()
                    .item(source.getItem() != null ? toSkuItem(source.getItem()) : null)
                    .subItem(source.getSubItem() != null ? toSkuSubItem(source.getSubItem()) : null)
                    .itemState(source.getItemState().getValue())
                    .build();
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static SkuItem toSkuItem(ru.shadewallcorp.itdepart.sampleService.topic.SkuItem source) {
            return SkuItem.builder()
                    .commerceId(source.getCommerceId())
                    .catalogId(source.getCatalogId() != null ? toCatalogId(source.getCatalogId()) : null)
                    .type(source.getType())
                    .frontName(source.getFrontName())
                    .mnp(source.getMnp())
                    .amount(source.getAmount())
                    .price(source.getPrice())
                    .salePrice(source.getSalePrice())
                    .build();
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static SkuSubItem toSkuSubItem(ru.shadewallcorp.itdepart.sampleService.topic.SkuSubItem source) {
            return SkuSubItem.builder()
                    .commerceId(source.getCommerceId())
                    .catalogId(source.getCatalogId() != null ? toCatalogId(source.getCatalogId()) : null)
                    .type(source.getType())
                    .frontName(source.getFrontName())
                    .amount(source.getAmount())
                    .price(source.getPrice())
                    .salePrice(source.getSalePrice())
                    .build();
        }

        @NotNull
        private static CatalogId toCatalogId(ru.shadewallcorp.itdepart.sampleService.topic.CatalogId source) {
            return CatalogId.builder()
                    .productId(source.getProductId())
                    .skuId(source.getSkuId())
                    .serialId(source.getSerialId())
                    .build();
        }

        @NotNull
        private static Payment toPayment(ru.shadewallcorp.itdepart.sampleService.topic.Payment source) {
            return Payment.builder()
                    .type(source.getType())
                    .state(source.getState())
                    .build();
        }

        @NotNull
        private static Delivery toDelivery(ru.shadewallcorp.itdepart.sampleService.topic.Delivery source) {
            return Delivery.builder()
                    .id(source.getId())
                    .name(source.getName())
                    .alias(source.getAlias())
                    .shipGroupType(source.getShipGroupType())
                    .cityId(source.getCityId())
                    .trackNumber(source.getTrackNumber())
                    .tariffZone(source.getTariffZone() != null ? toTariffZone(source.getTariffZone()) : null)
                    .salePoint(source.getSalePoint() != null ? toSalePoint(source.getSalePoint()) : null)
                    .courierInfo(source.getCourierInfo() != null ? toAddress(source.getCourierInfo()) : null)
                    .build();
        }

        @NotNull
        private static TariffZone toTariffZone(ru.shadewallcorp.itdepart.sampleService.topic.TariffZone source) {
            return TariffZone.builder()
                    .id(source.getId())
                    .name(source.getName())
                    .deliveryTimeDesc(source.getDeliveryTimeDesc())
                    .cost(source.getCost())
                    .build();
        }

        @NotNull
        private static SalePoint toSalePoint(ru.shadewallcorp.itdepart.sampleService.topic.SalePoint source) {
            return SalePoint.builder()
                    .id(source.getId())
                    .address(source.getAddress())
                    .build();
        }

        @NotNull
        private static Address toAddress(ru.shadewallcorp.itdepart.sampleService.topic.Address source) {
            return Address.builder()
                    .postalCode(source.getPostalCode())
                    .city(source.getCity())
                    .street(source.getStreet())
                    .building(source.getBuilding())
                    .apartment(source.getApartment())
                    .build();
        }

        @NotNull
        private static Client toClient(ru.shadewallcorp.itdepart.sampleService.topic.Client source) {
            return Client.builder()
                    .name(source.getName())
                    .gender(source.getGender())
                    .phone(source.getPhone())
                    .email(source.getEmail())
                    .identityCard(source.getIdentityCard() != null ? toIdentityCard(source.getIdentityCard()) : null)
                    .address(source.getAddress() != null ? toAddress(source.getAddress()) : null)
                    .build();
        }

        @NotNull
        private static IdentityCard toIdentityCard(ru.shadewallcorp.itdepart.sampleService.topic.IdentityCard source) {
            return IdentityCard.builder()
                    .type(source.getType())
                    .series(source.getSeries())
                    .number(source.getNumber())
                    .issuedBy(source.getIssuedBy())
                    .issuedOn(source.getIssuedOn())
                    .issuedCode(source.getIssuedCode())
                    .birthday(source.getBirthday())
                    .placeOfBirth(source.getPlaceOfBirth())
                    .build();
        }
    }

    /**
     * Конвертер классов из классы данных персистентной сущности в Kafka.
     */
    public static class CassandraToES {
        /**
         * Конвертировать заказ из заказа персистентной сущности в заказ ES.
         *
         * @param source заказ персистентной сущности
         * @return заказ ES.
         */
        @NotNull
        public static ru.shadewallcorp.itdepart.sampleService.elastic.Order toOrder(@NotNull Order source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.Order.builder()
                    .id(source.getId())
                    .creationDate(source.getCreationDate())
                    .creationDateOffset(source.getCreationDate().getOffset().getTotalSeconds())
                    .state(source.getState())
                    .type(source.getType())
                    .siteId(source.getSiteId())
                    .totalPrice(toMoney(source.getTotalPrice()))
                    .payment(source.getPayment() != null ? toPayment(source.getPayment()) : null)
                    .comment(source.getComment())
                    .mnp(source.getMnp())
                    .items(toOrderComposition(source.getItems()))
                    .promo(source.getPromo())
                    .parentNetwork(source.getParentNetwork())
                    .network(source.getNetwork())
                    .partner(source.getPartner())
                    .cancelReason(source.getCancelReason())
                    .delivery(source.getDelivery() != null ? toDelivery(source.getDelivery()) : null)
                    .client(source.getClient() != null ? toClient(source.getClient()) : null)
                    .build();
        }

        /**
         * Копировать список объектов.
         * @param source исходный список (@Nullable)
         * @return копия
         */
        @NotNull
        private static PSequence<ru.shadewallcorp.itdepart.sampleService.elastic.OrderCompositionItem> toOrderComposition(
                @Nullable PSequence<OrderCompositionItem> source) {
            return ofNullable(source)
                    .map(Collection::stream)
                    .map(stream -> stream.map(CassandraToES::toOrderCompositionItem).collect(toList()))
                    .map(TreePVector::from)
                    .orElse(TreePVector.empty());
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         * @return сконвертированный объект
         */
        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.OrderCompositionItem toOrderCompositionItem(
                OrderCompositionItem source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.OrderCompositionItem.builder()
                    .item(source.getItem() != null ? toSkuItem(source.getItem()) : null)
                    .subItem(source.getSubItem() != null ? toSkuSubItem(source.getSubItem()) : null)
                    .itemState(source.getItemState())
                    .build();
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.SkuItem toSkuItem(SkuItem source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.SkuItem.builder()
                    .commerceId(source.getCommerceId())
                    .catalogId(source.getCatalogId() != null ? toCatalogId(source.getCatalogId()) : null)
                    .type(source.getType())
                    .frontName(source.getFrontName())
                    .mnp(source.getMnp())
                    .amount(source.getAmount())
                    .price(source.getPrice() != null ? toMoney(source.getPrice()) : null)
                    .salePrice(source.getSalePrice() != null ? toMoney(source.getSalePrice()) : null)
                    .build();
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.SkuSubItem toSkuSubItem(SkuSubItem source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.SkuSubItem.builder()
                    .commerceId(source.getCommerceId())
                    .catalogId(source.getCatalogId() != null ? toCatalogId(source.getCatalogId()) : null)
                    .type(source.getType())
                    .frontName(source.getFrontName())
                    .amount(source.getAmount())
                    .price(source.getPrice() != null ? toMoney(source.getPrice()) : null)
                    .salePrice(source.getSalePrice() != null ? toMoney(source.getSalePrice()) : null)
                    .build();
        }

        /**
         * Конвертировать сумму в валюте из формата ES в формат библиотеки Money.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static Money toMoney(MonetaryAmount source) {
            return Money
                    .builder()
                    .amount(BigDecimal.valueOf(source.getNumber().doubleValue()))
                    .currency(source.getCurrency().toString())
                    .build();
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.CatalogId toCatalogId(CatalogId source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.CatalogId.builder()
                    .productId(source.getProductId())
                    .skuId(source.getSkuId())
                    .serialId(source.getSerialId())
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.Payment toPayment(Payment source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.Payment.builder()
                    .type(source.getType())
                    .state(source.getState())
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.Delivery toDelivery(Delivery source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.Delivery.builder()
                    .id(source.getId())
                    .name(source.getName())
                    .alias(source.getAlias())
                    .shipGroupType(source.getShipGroupType())
                    .cityId(source.getCityId())
                    .trackNumber(source.getTrackNumber())
                    .tariffZone(source.getTariffZone() != null ? toTariffZone(source.getTariffZone()) : null)
                    .salePoint(source.getSalePoint() != null ? toSalePoint(source.getSalePoint()) : null)
                    .courierInfo(source.getCourierInfo() != null ? toAddress(source.getCourierInfo()) : null)
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.TariffZone toTariffZone(TariffZone source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.TariffZone.builder()
                    .id(source.getId())
                    .name(source.getName())
                    .deliveryTimeDesc(source.getDeliveryTimeDesc())
                    .cost(source.getCost() != null ? toMoney(source.getCost()) : null)
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.SalePoint toSalePoint(SalePoint source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.SalePoint.builder()
                    .id(source.getId())
                    .address(source.getAddress())
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.Address toAddress(Address source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.Address.builder()
                    .postalCode(source.getPostalCode())
                    .city(source.getCity())
                    .street(source.getStreet())
                    .building(source.getBuilding())
                    .apartment(source.getApartment())
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.Client toClient(Client source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.Client.builder()
                    .name(source.getName())
                    .gender(source.getGender())
                    .phone(source.getPhone())
                    .email(source.getEmail())
                    .identityCard(source.getIdentityCard() != null ? toIdentityCard(source.getIdentityCard()) : null)
                    .address(source.getAddress() != null ? toAddress(source.getAddress()) : null)
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.elastic.IdentityCard toIdentityCard(
                IdentityCard source) {
            return ru.shadewallcorp.itdepart.sampleService.elastic.IdentityCard.builder()
                    .type(source.getType())
                    .series(source.getSeries())
                    .number(source.getNumber())
                    .issuedBy(source.getIssuedBy())
                    .issuedOn(source.getIssuedOn())
                    .issuedCode(source.getIssuedCode())
                    .birthday(source.getBirthday())
                    .placeOfBirth(source.getPlaceOfBirth())
                    .build();
        }
    }

}
