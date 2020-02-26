package ru.shadewallcorp.itdepart.sampleService.elastic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import ru.shadewallcorp.itdepart.sampleService.api.DeliveryCard;
import ru.shadewallcorp.itdepart.sampleService.api.FindOrdersResponse;
import ru.shadewallcorp.itdepart.sampleService.api.OrderCard;
import ru.shadewallcorp.itdepart.sampleService.topic.InitOrder;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import javax.money.MonetaryAmount;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Конвертеры типов из/в ElasticSearch.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public class Converters {

    /**
     * Конвертер классов ElasticSearch в API-классы.
     */
    public static class ESToApi {
        /**
         * Конвертировать заказ из ES в формат доменной модели сервиса заказов.
         * ES хранит дату с таймзоной, дает дату в формате ISO, но конвертирует в свой часовой пояс.
         *
         * @param orders     список заказов из ES.
         * @param totalPages количество страниц в результате.
         * @return заказ сервиса заказов.
         */
        @NotNull
        static FindOrdersResponse toFindOrdersResponse(List<Order> orders, long totalPages) {
            // Дата создания должна возвращаться с той же TZ, с которой записывалась. Эластик при выводе сам конвертирует TZ по серверу.
            return FindOrdersResponse.builder()
                    .orders(ofNullable(orders)
                            .map(list -> list.stream()
                                    .map(source -> ru.shadewallcorp.itdepart.sampleService.api.Order.builder()
                                            .id(source.getId())
                                            .creationDate(source.getCreationDate()
                                                    .withZoneSameInstant(ofNullable(source.getCreationDateOffset())
                                                            .map(ZoneOffset::ofTotalSeconds)
                                                            .orElse(ZoneOffset.UTC)))
                                            .state(source.getState())
                                            .type(source.getType())
                                            .siteId(source.getSiteId())
                                            .clientName(toClientName(source.getClient()))
                                            .totalPrice(source.getTotalPrice() != null ?
                                                    toMonetaryAmount(source.getTotalPrice()) : null)
                                            .payment(source.getPayment() != null ?
                                                    toPayment(source.getPayment()) : null)
                                            .comment(source.getComment())
                                            .mnp(source.getMnp())
                                            .delivery(source.getDelivery() != null ?
                                                    toDelivery(source.getDelivery()) : null)
                                            .build())
                                    .collect(toList()))
                            .map(TreePVector::from)
                            .orElse(TreePVector.empty()))
                    .totalPages(totalPages)
                    .build();
        }

        /**
         * Конвертировать заказ из ES в формат доменной модели сервиса заказов.
         * ES хранит дату с таймзоной, дает дату в формате ISO, но конвертирует в свой часовой пояс.
         *
         * @param source заказ из ES.
         * @return заказ сервиса заказов.
         */
        @NotNull
        public static OrderCard toOrderCard(@NotNull Order source) {
            // Дата создания должна возвращаться с той же TZ, с которой записывалась. Эластик при выводе сам конвертирует TZ по серверу.
            ZoneOffset zoneDateOffset = ofNullable(source.getCreationDateOffset())
                    .map(ZoneOffset::ofTotalSeconds)
                    .orElse(ZoneOffset.UTC);
            return OrderCard.builder()
                    .id(source.getId())
                    .creationDate(source.getCreationDate().withZoneSameInstant(zoneDateOffset))
                    .state(source.getState())
                    .type(source.getType())
                    .siteId(source.getSiteId())
                    .totalPrice(source.getTotalPrice() != null ? toMonetaryAmount(source.getTotalPrice()) : null)
                    .payment(source.getPayment() != null ? toPayment(source.getPayment()) : null)
                    .comment(source.getComment())
                    .mnp(source.getMnp())
                    .items(toOrderComposition(source.getItems()))
                    .promo(source.getPromo())
                    .parentNetwork(source.getParentNetwork())
                    .network(source.getNetwork())
                    .partner(source.getPartner())
                    .cancelReason(source.getCancelReason())
                    .deliveryCard(source.getDelivery() != null ? toDeliveryCard(source.getDelivery()) : null)
                    .client(source.getClient() != null ? toClient(source.getClient()) : null)
                    .build();
        }

        /**
         * Копировать список объектов.
         *
         * @param source исходный список (@Nullable)
         * @return копия
         */
        @NotNull
        private static PSequence<ru.shadewallcorp.itdepart.sampleService.api.OrderCompositionItem> toOrderComposition(
                @Nullable List<OrderCompositionItem> source) {
            return ofNullable(source)
                    .map(Collection::stream)
                    .map(stream -> stream.map(ESToApi::toOrderCompositionItem).collect(toList()))
                    .map(TreePVector::from)
                    .orElse(TreePVector.empty());
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.OrderCompositionItem toOrderCompositionItem(
                OrderCompositionItem source) {
            return ru.shadewallcorp.itdepart.sampleService.api.OrderCompositionItem.builder()
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
        private static ru.shadewallcorp.itdepart.sampleService.api.SkuItem toSkuItem(SkuItem source) {
            return ru.shadewallcorp.itdepart.sampleService.api.SkuItem.builder()
                    .commerceId(source.getCommerceId())
                    .catalogId(source.getCatalogId() != null ? toCatalogId(source.getCatalogId()) : null)
                    .type(source.getType())
                    .frontName(source.getFrontName())
                    .mnp(source.getMnp())
                    .amount(source.getAmount())
                    .price(source.getPrice() != null ? toMonetaryAmount(source.getPrice()) : null)
                    .salePrice(source.getSalePrice() != null ? toMonetaryAmount(source.getSalePrice()) : null)
                    .build();
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.SkuSubItem toSkuSubItem(SkuSubItem source) {
            return ru.shadewallcorp.itdepart.sampleService.api.SkuSubItem.builder()
                    .commerceId(source.getCommerceId())
                    .catalogId(source.getCatalogId() != null ? toCatalogId(source.getCatalogId()) : null)
                    .type(source.getType())
                    .frontName(source.getFrontName())
                    .amount(source.getAmount())
                    .price(source.getPrice() != null ? toMonetaryAmount(source.getPrice()) : null)
                    .salePrice(source.getSalePrice() != null ? toMonetaryAmount(source.getSalePrice()) : null)
                    .build();
        }

        /**
         * Конвертировать сумму в валюте из формата ES в формат библиотеки Money.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static MonetaryAmount toMonetaryAmount(Money source) {
            return org.javamoney.moneta.Money.of(source.getAmount(), source.getCurrency());
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.CatalogId toCatalogId(CatalogId source) {
            return ru.shadewallcorp.itdepart.sampleService.api.CatalogId.builder()
                    .productId(source.getProductId())
                    .skuId(source.getSkuId())
                    .serialId(source.getSerialId())
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.Payment toPayment(Payment source) {
            return ru.shadewallcorp.itdepart.sampleService.api.Payment.builder()
                    .type(source.getType())
                    .state(source.getState())
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.Delivery toDelivery(Delivery source) {
            return ru.shadewallcorp.itdepart.sampleService.api.Delivery.builder()
                    .id(source.getId())
                    .name(source.getName())
                    .shipGroupType(source.getShipGroupType())
                    .cityId(source.getCityId())
                    .salePointId(source.getSalePoint() != null ? source.getSalePoint().getId() : null)
                    .salePointAddress(source.getSalePoint() != null ? source.getSalePoint().getAddress() : null)
                    .trackNumber(source.getTrackNumber())
                    .build();
        }

        @NotNull
        private static DeliveryCard toDeliveryCard(Delivery source) {
            return DeliveryCard.builder()
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
        private static ru.shadewallcorp.itdepart.sampleService.api.TariffZone toTariffZone(TariffZone source) {
            return ru.shadewallcorp.itdepart.sampleService.api.TariffZone.builder()
                    .id(source.getId())
                    .name(source.getName())
                    .deliveryTimeDesc(source.getDeliveryTimeDesc())
                    .cost(source.getCost() != null ? toMonetaryAmount(source.getCost()) : null)
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.SalePoint toSalePoint(SalePoint source) {
            return ru.shadewallcorp.itdepart.sampleService.api.SalePoint.builder()
                    .id(source.getId())
                    .address(source.getAddress())
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.Address toAddress(Address source) {
            return ru.shadewallcorp.itdepart.sampleService.api.Address.builder()
                    .postalCode(source.getPostalCode())
                    .city(source.getCity())
                    .street(source.getStreet())
                    .building(source.getBuilding())
                    .apartment(source.getApartment())
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.Client toClient(Client source) {
            return ru.shadewallcorp.itdepart.sampleService.api.Client.builder()
                    .name(source.getName())
                    .gender(source.getGender())
                    .phone(source.getPhone())
                    .email(source.getEmail())
                    .identityCard(source.getIdentityCard() != null ? toIdentityCard(source.getIdentityCard()) : null)
                    .address(source.getAddress() != null ? toAddress(source.getAddress()) : null)
                    .build();
        }

        @NotNull
        private static ru.shadewallcorp.itdepart.sampleService.api.IdentityCard toIdentityCard(IdentityCard source) {
            return ru.shadewallcorp.itdepart.sampleService.api.IdentityCard.builder()
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

        @Nullable
        private static String toClientName(Client source) {
            return ofNullable(source).map(Client::getName).orElse(null);
        }
    }

    /**
     * Конвертер классов Kafka в ElasticSearch .
     */
    public static class KafkaToEs {
        /**
         * Конвертировать заказ из Kafka в формат заказов ElasticSearch.
         *
         * @param source заказ из Kafka.
         * @return заказ ElasticSearch.
         */
        @NotNull
        public static Order toOrder(@NotNull InitOrder source) {
            return Order.builder()
                    .id(source.getId())
                    .creationDate(source.getCreationDate())
                    .creationDateOffset(source.getCreationDate().getOffset().getTotalSeconds())
                    .state(source.getState())
                    .type(source.getType())
                    .siteId(source.getSiteId())
                    .totalPrice(source.getTotalPrice() != null ? toMoney(source.getTotalPrice()) : null)
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
         *
         * @param source исходный список (@Nullable)
         * @return копия
         */
        @NotNull
        private static PSequence<OrderCompositionItem> toOrderComposition(
                @Nullable PSequence<ru.shadewallcorp.itdepart.sampleService.topic.OrderCompositionItem> source) {
            return ofNullable(source)
                    .map(Collection::stream)
                    .map(stream -> stream.map(KafkaToEs::toOrderCompositionItem).collect(toList()))
                    .map(TreePVector::from)
                    .orElse(TreePVector.empty());
        }

        /**
         * Конвертировать объект в такой же, но другого слоя.
         *
         * @return сконвертированный объект
         */
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
        private static SkuSubItem toSkuSubItem(ru.shadewallcorp.itdepart.sampleService.topic.SkuSubItem source) {
            return SkuSubItem.builder()
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
         * Конвертация суммы в валюте.
         * @param source сумма в валюте из библиотеки Money.
         * @return сумма в валюте для ES
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
                    .cost(source.getCost() != null ? toMoney(source.getCost()) : null)
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

}
